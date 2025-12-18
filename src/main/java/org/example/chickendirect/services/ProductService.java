package org.example.chickendirect.services;

import org.example.chickendirect.dtos.ProductDto;
import org.example.chickendirect.entities.Product;
import org.example.chickendirect.enums.ProductStatus;
import org.example.chickendirect.repos.ProductRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.example.chickendirect.services.OrderService.LOW_STOCK_THRESHOLD;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepo productRepo;

    public ProductService(ProductRepo productRepo) {
        this.productRepo = productRepo;
    }

    public Product createProduct(ProductDto productDto){
        log.info("Creating product with name='{}'", productDto.name());

        Optional<Product> existingProduct = productRepo.findByName(productDto.name());
        if (existingProduct.isPresent()){
            log.warn("Product already exists: {}", productDto.name());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product already exists: " + productDto.name());
        }

        Product newProduct = new Product();
        newProduct.setName(productDto.name());
        newProduct.setDescription(productDto.description());
        newProduct.setPrice(productDto.price());
        newProduct.setQuantity(productDto.quantity());
        newProduct.setUnit(productDto.unit());

        ProductStatus status = determineStatusByQuantity(productDto.quantity());
        newProduct.setProductStatus(status);

        Product savedProduct = productRepo.save(newProduct);
        log.info("Product '{}' created successfully with id={}", savedProduct.getName(), savedProduct.getProductId());
        return savedProduct;
    }

    private ProductStatus determineStatusByQuantity(int quantity) {
        if (quantity == 0) return ProductStatus.OUT_OF_STOCK;
        if (quantity <= LOW_STOCK_THRESHOLD) return ProductStatus.PENDING_RESTOCK;
        return ProductStatus.IN_STOCK;
    }

    public List<Product> createProducts(List<ProductDto> productDtos) {
        log.info("Creating {} products", productDtos.size());

        List<Product> createdProducts = productDtos.stream()
                .map(dto -> {
                    try{
                        return createProduct(dto);
                    } catch (ResponseStatusException ex){
                        log.error("Failed to create product '{}' : {}", dto.name(), ex.getReason());
                        throw ex;
                    }
                })
                .toList();

        log.info("All {} products created successfully", createdProducts.size());
                return createdProducts;
    }

    public Product updateProductStatus(String name, ProductStatus newStatus){
        if(newStatus == null){
            log.warn("Missing 'newStatus' in request");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request must include 'newStatus'");
        }

        log.info("Updating status for product with name='{}' to {} ", name, newStatus);

        Product product = productRepo.findByName(name)
                .orElseThrow(() -> {
                    log.warn("Product not found with name='{}'", name);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + name);
                });

        product.setProductStatus(newStatus);
        log.info("Product '{}' status updated successfully to {}", name, newStatus);
        return productRepo.save(product);
    }

    public Product updateProductPrice(String name, BigDecimal newPrice){
        if(newPrice == null){
            log.warn("Missing 'newPrice' in request");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request must include 'newPrice'");
        }
        if(newPrice.compareTo(BigDecimal.ZERO) <= 0 ){
            log.warn("Invalid newPrice provided for product '{}' : {} ", name, newPrice);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price must be a positive value");
        }

        log.info("Updating price for product with name='{}' to {}", name, newPrice);

        Product product = productRepo.findByName(name)
                .orElseThrow(() -> {
                    log.warn("Product not found with name='{}'", name);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + name);
                });

                product.setPrice(newPrice);
                log.info("Product '{}' price updated successfully to {}", name, newPrice);
                return productRepo.save(product);
    }

    public List<Product> findAllProducts(){
        log.info("Fetching all products");

        List<Product> products = productRepo.findAll();

        if(products.isEmpty()){
            log.warn("No products found in the database");
        } else {
            log.info("Fetched {} products", products.size());
        }
        return products;
    }

    public Product findProductById(long id){
        log.info("Fetching product with id={}", id);
        return productRepo.findById(id)
                .orElseThrow(() ->  {
                    log.warn("Product not found with id={}", id);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Product not found with id " + id);
                });
    }


    public Product updateProductQuantity(long productId, Integer newQuantity) {
        if(newQuantity == null){
            log.warn("Missing 'newQuantity' in request");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request must include 'newQuantity'");
        }

        log.info("Updating quantity for product with id={} to {}", productId, newQuantity);

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product not found with id={}", productId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
                });

        product.setQuantity(newQuantity);

        if (newQuantity <= 0) {
            product.setProductStatus(ProductStatus.OUT_OF_STOCK);
            log.info("Product id={} is now OUT_OF_STOCK", productId);
        } else if (newQuantity <= OrderService.getLowStockThreshold()){
            product.setProductStatus(ProductStatus.PENDING_RESTOCK);
            log.info("Product id={} is now PENDING_RESTOCK (low in stock)", productId);
        } else{
            product.setProductStatus(ProductStatus.IN_STOCK);
            log.info("Product id={} is now IN_STOCK", productId);
        }

        Product updatedProduct = productRepo.save(product);
        log.info("Product id={} updated successfully", productId);

        return productRepo.save(updatedProduct);
    }

    public void deleteProductById(long id){
        log.info("Attempting to delete product with id={}", id);

        if (!productRepo.existsById(id)) {
            log.warn("Product not found with id={}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id " + id);
        }
        productRepo.deleteById(id);
        log.info("Product with id={} deleted successfully", id);
    }

}


