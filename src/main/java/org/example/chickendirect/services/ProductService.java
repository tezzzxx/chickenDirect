package org.example.chickendirect.services;

import org.example.chickendirect.dtos.ProductDto;
import org.example.chickendirect.entities.Product;
import org.example.chickendirect.repos.ProductRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepo productRepo;

    public ProductService(ProductRepo productRepo) {
        this.productRepo = productRepo;
    }

    public Product createProduct(ProductDto productDto){
        var newProduct = new Product(
                productDto.name(),
                productDto.description(),
                productDto.price(),
                productDto.productStatus(),
                productDto.quantity()
        );
        return productRepo.save(newProduct);
    }

    public List<Product> findAllProducts(){
        return productRepo.findAll();
    }

    public Product findProductById(long id){
        return productRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found with id " + id));
    }

    public void deleteProductById(long id){
        if (!productRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id " + id);
        }
        productRepo.deleteById(id);
    }

    public Product saveProduct(Product product){
        return productRepo.save(product);
    }
}
