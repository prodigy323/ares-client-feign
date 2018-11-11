package com.example.aresclientfeign;

import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@org.springframework.cloud.openfeign.FeignClient("ares-service-h2")
public interface FeignClient {

    @RequestMapping(path = "/heroes", method = RequestMethod.GET)
    Resources<Hero> getHeroes();

}
