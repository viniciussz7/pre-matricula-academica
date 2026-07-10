package br.edu.uesb.prematricula.admin.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.edu.uesb.prematricula.admin.exception.AdminNotFoundException;
import br.edu.uesb.prematricula.admin.model.dto.request.CreateAdminRequestDTO;
import br.edu.uesb.prematricula.admin.model.dto.response.AdminResponseDTO;
import br.edu.uesb.prematricula.admin.model.entity.Admin;
import br.edu.uesb.prematricula.admin.repository.AdminRepository;
import br.edu.uesb.prematricula.user.model.dto.request.CreateUserRequestDTO;
import br.edu.uesb.prematricula.user.model.entity.User;
import br.edu.uesb.prematricula.user.model.enums.UserRole;
import br.edu.uesb.prematricula.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserService userService;

    @Transactional
    public AdminResponseDTO create(CreateAdminRequestDTO dto) {

        CreateUserRequestDTO userDTO = new CreateUserRequestDTO(
                dto.fullName(),
                dto.email(),
                UserRole.ADMIN);

        User user = userService.createUser(userDTO);

        Admin admin = Admin.builder()
                .user(user)
                .build();

        Admin saveAdmin = adminRepository.save(admin);

        return toResponse(saveAdmin);

    }

    public List<AdminResponseDTO> findAll() {

        return adminRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();

    }

    public AdminResponseDTO findById(UUID id) {

        Admin admin = getAdmin(id);

        return toResponse(admin);
    }
    

    private Admin getAdmin(UUID id) {

        return adminRepository.findById(id)
                .orElseThrow(() -> new AdminNotFoundException("Admin not found."));
    }

    // update()?

    private AdminResponseDTO toResponse(Admin admin) {

        return new AdminResponseDTO(
                admin.getId(),
                admin.getUser().getId(),
                admin.getUser().getFullName(),
                admin.getUser().getEmail(),
                admin.isActive());

    }

}
