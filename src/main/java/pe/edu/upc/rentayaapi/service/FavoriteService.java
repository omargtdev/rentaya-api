package pe.edu.upc.rentayaapi.service;

import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.rentayaapi.dto.PropertyResponse;
import pe.edu.upc.rentayaapi.exception.ApiException;
import pe.edu.upc.rentayaapi.model.Favorite;
import pe.edu.upc.rentayaapi.model.Property;
import pe.edu.upc.rentayaapi.model.User;
import pe.edu.upc.rentayaapi.repository.FavoriteRepository;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final PropertyService propertyService;
    private final CurrentUserService currentUserService;

    public FavoriteService(
        FavoriteRepository favoriteRepository,
        PropertyService propertyService,
        CurrentUserService currentUserService
    ) {
        this.favoriteRepository = favoriteRepository;
        this.propertyService = propertyService;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> list() {
        User user = currentUserService.get();
        return favoriteRepository.findByUserIdOrderByIdDesc(user.getId()).stream()
            .map(Favorite::getProperty)
            .map(propertyService::toResponse)
            .toList();
    }

    @Transactional
    public void add(Integer propertyId) {
        User user = currentUserService.get();
        Property property = propertyService.find(propertyId);
        if (favoriteRepository.existsByUserIdAndPropertyId(user.getId(), propertyId)) {
            throw new ApiException(HttpStatus.CONFLICT, "La propiedad ya está en favoritos");
        }

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setProperty(property);
        try {
            favoriteRepository.saveAndFlush(favorite);
        } catch (DataIntegrityViolationException ex) {
            throw new ApiException(HttpStatus.CONFLICT, "La propiedad ya está en favoritos");
        }
    }

    @Transactional
    public void remove(Integer propertyId) {
        User user = currentUserService.get();
        favoriteRepository.findByUserIdAndPropertyId(user.getId(), propertyId)
            .ifPresent(favoriteRepository::delete);
    }
}
