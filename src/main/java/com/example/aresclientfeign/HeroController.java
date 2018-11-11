package com.example.aresclientfeign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@Slf4j
@RequestMapping("/hero")
public class HeroController {

    @Autowired
    private FeignClient feignClient;

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public Collection<Hero> getHero() {
        return feignClient.getHeroes().getContent();
    }

    // TODO: CRUD operations

    // TODO: UI javascript to present data
}
