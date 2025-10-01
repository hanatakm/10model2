package com.model2.mvc.web.product;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.model2.mvc.common.Search;
import com.model2.mvc.service.domain.Product;
import com.model2.mvc.service.product.ProductService;

@RestController
@RequestMapping("/product/json")
public class ProductRestController {

    @Autowired @Qualifier("productServiceImpl")
    private ProductService productService;

    @PostMapping("/addProduct")
    public boolean addProduct(@RequestBody Product p) throws Exception {
        productService.addProduct(p);
        return true;
    }

    @GetMapping("/getProduct/{prodNo}")
    public Product getProduct(@PathVariable int prodNo) throws Exception {
        return productService.getProduct(prodNo);
    }

    @PostMapping("/getProductList")
    public Map<String,Object> getProductList(@RequestBody Search s) throws Exception {
        return productService.getProductList(s);
    }

    @GetMapping("/findProduct")
    public List<Product> findProduct(@RequestParam String prodNoKeyword) throws Exception {
        return productService.findProduct(prodNoKeyword);
    }

    @PostMapping("/updateProduct") // PUT 으로 바꿔도 OK
    public boolean updateProduct(@RequestBody Product p) throws Exception {
        productService.updateProduct(p);
        return true;
    }

    @DeleteMapping("/removeProduct/{prodNo}") // POST 받으려면 @PostMapping 으로 변경
    public boolean remove(@PathVariable int prodNo) throws Exception {
        productService.removeProduct(prodNo);
        return true;
    }
}
