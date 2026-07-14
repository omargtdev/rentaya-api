package pe.edu.upc.rentayaapi.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.rentayaapi.dto.PropertyRequest;
import pe.edu.upc.rentayaapi.dto.PropertyResponse;
import pe.edu.upc.rentayaapi.exception.ApiException;
import pe.edu.upc.rentayaapi.model.Photo;
import pe.edu.upc.rentayaapi.model.Property;
import pe.edu.upc.rentayaapi.model.Rol;
import pe.edu.upc.rentayaapi.model.User;
import pe.edu.upc.rentayaapi.repository.PhotoRepository;
import pe.edu.upc.rentayaapi.repository.PropertyRepository;

@Service
public class PropertyService {

    private static final String AVAILABLE = "Disponible";
    private static final String INACTIVE = "Inactivo";

    private final PropertyRepository propertyRepository;
    private final PhotoRepository photoRepository;
    private final CurrentUserService currentUserService;

    public PropertyService(
        PropertyRepository propertyRepository,
        PhotoRepository photoRepository,
        CurrentUserService currentUserService
    ) {
        this.propertyRepository = propertyRepository;
        this.photoRepository = photoRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> list(String district, BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El precio mínimo no puede ser mayor al máximo");
        }
        String normalizedDistrict = district == null || district.isBlank() ? null : district.trim();
        Specification<Property> filters = availableProperties();
        if (normalizedDistrict != null) {
            String districtValue = normalizedDistrict.toLowerCase(Locale.ROOT);
            filters = filters.and((root, query, builder) ->
                builder.equal(builder.lower(root.<String>get("district")), districtValue));
        }
        if (minPrice != null) {
            filters = filters.and((root, query, builder) ->
                builder.greaterThanOrEqualTo(root.<BigDecimal>get("price"), minPrice));
        }
        if (maxPrice != null) {
            filters = filters.and((root, query, builder) ->
                builder.lessThanOrEqualTo(root.<BigDecimal>get("price"), maxPrice));
        }

        Sort newestFirst = Sort.by(
            Sort.Order.desc("createdAt"),
            Sort.Order.desc("id")
        );
        return propertyRepository.findAll(filters, newestFirst).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public PropertyResponse get(Integer id) {
        return toResponse(find(id));
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> mine() {
        User owner = currentUserService.get();
        requireOwnerRole(owner);
        return propertyRepository.findByOwnerIdOrderByCreatedAtDescIdDesc(owner.getId()).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public PropertyResponse create(PropertyRequest request) {
        User owner = currentUserService.get();
        requireOwnerRole(owner);

        Property property = new Property();
        property.setOwner(owner);
        property.setStatus(AVAILABLE);
        apply(property, request);
        Property saved = propertyRepository.saveAndFlush(property);
        replacePhotos(saved, request.photos());
        return toResponse(saved);
    }

    @Transactional
    public PropertyResponse update(Integer id, PropertyRequest request) {
        User owner = currentUserService.get();
        Property property = find(id);
        requireOwnership(property, owner);
        apply(property, request);
        Property saved = propertyRepository.saveAndFlush(property);
        replacePhotos(saved, request.photos());
        return toResponse(saved);
    }

    @Transactional
    public void deactivate(Integer id) {
        User owner = currentUserService.get();
        Property property = find(id);
        requireOwnership(property, owner);
        property.setStatus(INACTIVE);
        propertyRepository.save(property);
    }

    @Transactional(readOnly = true)
    public Property find(Integer id) {
        return propertyRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Propiedad no encontrada"));
    }

    @Transactional(readOnly = true)
    public PropertyResponse toResponse(Property property) {
        List<String> photos = photoRepository.findByPropertyIdOrderById(property.getId()).stream()
            .map(Photo::getUrl)
            .toList();
        return new PropertyResponse(
            property.getId(),
            property.getOwner().getId(),
            fullName(property.getOwner()),
            property.getTitle(),
            property.getDistrict(),
            property.getAddress(),
            property.getPrice(),
            property.getBedrooms(),
            property.getBathrooms(),
            property.getArea(),
            property.getDescription(),
            photos,
            property.getStatus(),
            property.getCreatedAt()
        );
    }

    private void apply(Property property, PropertyRequest request) {
        property.setTitle(request.title().trim());
        property.setDistrict(request.district().trim());
        property.setAddress(request.address().trim());
        property.setPrice(request.price());
        property.setBedrooms(request.rooms());
        property.setBathrooms(request.bathrooms());
        property.setArea(request.area());
        property.setDescription(request.description().trim());
    }

    private Specification<Property> availableProperties() {
        return (root, query, builder) -> builder.equal(root.get("status"), AVAILABLE);
    }

    private void replacePhotos(Property property, List<String> urls) {
        photoRepository.deleteByPropertyId(property.getId());
        List<Photo> photos = urls.stream().map(url -> {
            Photo photo = new Photo();
            photo.setProperty(property);
            photo.setUrl(url.trim());
            return photo;
        }).toList();
        photoRepository.saveAll(photos);
    }

    private void requireOwnerRole(User user) {
        if (user.getRole() != Rol.PROPIETARIO) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Solo los propietarios pueden gestionar propiedades");
        }
    }

    private void requireOwnership(Property property, User user) {
        requireOwnerRole(user);
        if (!property.getOwner().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No eres dueño de esta propiedad");
        }
    }

    static String fullName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}
