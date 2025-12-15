package org.example.chickendirect.services;

import org.example.chickendirect.dtos.ProductDto;
import org.example.chickendirect.entities.Product;
import org.example.chickendirect.enums.ProductStatus;
import org.example.chickendirect.repos.ProductRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepo productRepo;

    public ProductService(ProductRepo productRepo) {
        this.productRepo = productRepo;
    }

    public Product createProduct(ProductDto productDto){

        Optional<Product> existingProduct = productRepo.findByName(productDto.name());

        if (existingProduct.isPresent()){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product already exists: " + productDto.name());
        }

        Product newProduct = new Product();
        newProduct.setName(productDto.name());
        newProduct.setDescription(productDto.description());
        newProduct.setPrice(productDto.price());
        newProduct.setProductStatus(productDto.productStatus());
        newProduct.setQuantity(productDto.quantity());
        newProduct.setUnit(productDto.unit());

        return productRepo.save(newProduct);
    }

    public List<Product> createProducts(List<ProductDto> productDtos) {
        return productDtos.stream()
                .map(this::createProduct)
                .toList();
    }

    public Product updateProductStatus(String name, ProductStatus newStatus){
        Product product = productRepo.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + name));

        product.setProductStatus(newStatus);
        return productRepo.save(product);
    }

    public Product updateProductPrice(String name, BigDecimal newPrice){
        Product product = productRepo.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + name));

                product.setPrice(newPrice);
                return productRepo.save(product);
    }

    public List<Product> findAllProducts(){
        return productRepo.findAllByOrderByProductIdAsc();
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

}
