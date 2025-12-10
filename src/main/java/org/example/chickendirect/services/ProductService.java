package org.example.chickendirect.services;

import org.example.chickendirect.dtos.ProductDto;
import org.example.chickendirect.entities.Product;
import org.example.chickendirect.repos.ProductRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
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

   /* public Product updateProduct(ProductDto productDto){

        Product product = productRepo.findByName(productDto.name());


    }*/

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
