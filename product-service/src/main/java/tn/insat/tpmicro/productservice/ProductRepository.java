package tn.insat.tpmicro.productservice;

import org.springframework.data.jpa.repository.JpaRepository;


public interface ProductRepository extends JpaRepository<Product , Integer> {
}