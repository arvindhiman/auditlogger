package org.example.audit;

import io.micrometer.core.annotation.Timed;
import org.example.audit.model.Movie;
import org.example.audit.model.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MovieController {

    @Autowired
    MovieRepository movieRepository;

    @GetMapping("/v1/movies")
    @Timed
    public Iterable<Movie> getAllMovies() {

        return movieRepository.findAll();
    }


}
