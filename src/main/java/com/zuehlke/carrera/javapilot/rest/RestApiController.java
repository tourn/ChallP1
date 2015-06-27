package com.zuehlke.carrera.javapilot.rest;

import com.zuehlke.carrera.javapilot.services.KobayashiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/")
    public class RestApiController {

    public KobayashiService service;

    @RequestMapping(value="/up", method = RequestMethod.GET,  produces = "application/json")
    public String getTrack() {
        return "ok";
    }


}
