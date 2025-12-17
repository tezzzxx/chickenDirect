package org.example.chickendirect.controllers;

import org.example.chickendirect.dtos.ProductDto;
import org.example.chickendirect.dtos.UpdateProductPrice;
import org.example.chickendirect.dtos.UpdateProductQuantity;
import org.example.chickendirect.dtos.UpdateProductStatus;
import org.example.chickendirect.entities.Product;
import org.example.chickendirect.services.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);


    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody ProductDto productDto){
        log.info("Received request to create product with name='{}'", productDto.name());
        Product createdProduct = productService.createProduct(productDto);
        log.info("Product '{}' created successfully with id={}", createdProduct.getName(), createdProduct.getProductId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<Product>> createProducts(@RequestBody List<ProductDto> productDtos){
        log.info("Received request to create {} products", productDtos.size());
        List<Product> createdProducts = productService.createProducts(productDtos);
        log.info("All {} products created successfully", createdProducts.size());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProducts);
    }

    @PatchMapping("/{name}/status")
    public ResponseEntity<Product> updateProductStatus(
            @PathVariable String name,
            @RequestBody UpdateProductStatus request
    ){
        log.info("Received request to update status for product '{}'", name);
        Product updatedStatus = productService.updateProductStatus(name, request.newStatus());
        log.info("Product '{}' status updated to {}", name, updatedStatus.getProductStatus());
        return ResponseEntity.ok(updatedStatus);
    }

    @PatchMapping("/{name}/price")
    public ResponseEntity<Product> updateProductPrice(
            @PathVariable String name,
            @RequestBody UpdateProductPrice request
            ) {
        log.info("Received request to update price for product '{}'", name);
        Product updatedPrice = productService.updateProductPrice(name, request.newPrice());
        log.info("Product '{}' price updated to {}", name, updatedPrice.getPrice());
        return ResponseEntity.ok(updatedPrice);
    }

    @PatchMapping("/{id}/quantity")
    public ResponseEntity<Product> updateProductQuantity(
            @PathVariable long id,
            @RequestBody UpdateProductQuantity request
    ) {
        log.info("Received request to update quantity for product id={}", id);
        Product updatedQuantity = productService.updateProductQuantity(id, request.newQuantity());
        log.info("Product id={} quantity updated to {}", id, updatedQuantity.getQuantity());
        return ResponseEntity.ok(updatedQuantity);
    }

    @GetMapping
    public ResponseEntity<List<Product>> findAllProducts(){
        return ResponseEntity.ok(productService.findAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> findProductById(@PathVariable long id){
        log.info("Fetching product with id={}", id);
        return ResponseEntity.ok(productService.findProductById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProductById(@PathVariable long id){
        log.info("Deleting product with id={}", id);
        productService.deleteProductById(id);
        log.info("Product with id={} deleted successfully", id);
        return ResponseEntity.ok("Product deleted");
    }



}
