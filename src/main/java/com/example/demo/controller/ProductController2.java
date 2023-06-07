package com.example.demo.controller;


import com.example.demo.Entity.Product;
import com.example.demo.parameter.ProductQueryParameter;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductController2 {
    private final List<Product> productDB = new ArrayList<>();

    @PostConstruct
    private void initDB() {
        productDB.add(new Product("B0001", "Android Development (Java)", 380));
        productDB.add(new Product("B0002", "Android Development (Kotlin)", 420));
        productDB.add(new Product("B0003", "Data Structure (Java)", 250));
        productDB.add(new Product("B0004", "Finance Management", 450));
        productDB.add(new Product("B0005", "Human Resource Management", 330));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable("id") String id) {
        Optional<Product> productOp = productDB.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();

        if (productOp.isPresent()) {
            Product product = productOp.get();
            return ResponseEntity.ok().body(product);
        } else {
            return ResponseEntity.notFound().build();
        }


    }
    /*

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts(
            @RequestParam(value = "keyword",defaultValue = "") String name){
        List<Product> products = productDB.stream()
                .filter(p -> p.getName().toUpperCase().contains(name.toUpperCase()))
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(products);
    }
*/


    @GetMapping
    public ResponseEntity<List<Product>> getProducts(@ModelAttribute ProductQueryParameter param){
        String keyword = param.getKeyword();
        String orderBy = param.getOrderBy();
        String sortRule = param.getSortRule();
        Comparator<Product> comparator = genSortComparator(orderBy, sortRule);

        List<Product> products = productDB.stream()
                .filter(p -> p.getName().toUpperCase().contains(keyword.toUpperCase()))
                .sorted(comparator)
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(products);
    }



    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product request) {
        boolean isIdDuplicated = productDB.stream()
                .anyMatch(p -> p.getId().equals(request.getId()));
        if (isIdDuplicated) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        }

        Product product = new Product();
        product.setId(request.getId());
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        productDB.add(product);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(product.getId())
                .toUri();

        return ResponseEntity.created(location).body(product);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Product> replaceProduct(
            @PathVariable("id") String id, @RequestBody Product request) {
        Optional<Product> productOp = productDB.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();

        if (productOp.isPresent()) {
            Product product = productOp.get();
            product.setName(request.getName());
            product.setPrice(request.getPrice());

            return ResponseEntity.ok().body(product);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Product> deleteProduct(
            @PathVariable("id") String id){
        boolean isRemoved = productDB.removeIf(p -> p.getId().equals(id));

        return isRemoved
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    private Comparator<Product> genSortComparator(String orderBy, String sortRule){
        Comparator<Product> comparator = (p1, p2) -> 0;
        if (Objects.isNull(orderBy) || (Objects.isNull(sortRule))){
            return comparator;
        }

        if (orderBy.equalsIgnoreCase("price")){
            comparator = Comparator.comparing(Product::getPrice);
        } else if ( orderBy.equalsIgnoreCase("name")) {
            comparator = Comparator.comparing(Product::getName);
        }

        return sortRule.equalsIgnoreCase("desc")
                ? comparator.reversed()
                : comparator;
    }

}

/*

fromCurrentRequest：以目前呼叫的資源路徑為基礎來建立 URI，此處為「http://…/products」。
path：以目前的資源路徑再做延伸，定義新的路徑格式，可加入佔位符，此處為「http://…/products/{id}」。
buildAndExpand：將參數填入路徑中的佔位符，產生真實的資源路徑，此處為「http://…/products/實際產品編號」。

 */