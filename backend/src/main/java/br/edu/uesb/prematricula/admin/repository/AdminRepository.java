package br.edu.uesb.prematricula.admin.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.uesb.prematricula.admin.model.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, UUID> {

    
}