package ec.com.reactive.music.service.impl;

import ec.com.reactive.music.domain.entities.Playlist;
import ec.com.reactive.music.repository.IPlaylistRepository;
import ec.com.reactive.music.repository.ISongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
@ExtendWith(MockitoExtension.class)
class PlaylistServiceImplTest {

    @Mock
    IPlaylistRepository playlistRepository;

    @Mock
    ISongRepository songRepository;

    ModelMapper modelMapper;

    PlaylistServiceImpl service;

    @BeforeEach
    void init(){
        service = new PlaylistServiceImpl(playlistRepository,songRepository,modelMapper);
    }

    @Test
    void deletePlaylist() {

        //Arrange
        Playlist playlistExpected = new Playlist();
        playlistExpected.setIdPlaylist("1618");
        playlistExpected.setName("Ralsei's Songs");
        playlistExpected.setUsername("Solaris Magnum Leo");
        ResponseEntity<String> responseDelete = new ResponseEntity<>(playlistExpected.getIdPlaylist(), HttpStatus.ACCEPTED);

        Mockito.when(playlistRepository.findById(Mockito.any(String.class)))
                .thenReturn(Mono.just(playlistExpected));
        Mockito.when(playlistRepository.deleteById(Mockito.any(String.class)))
                .thenReturn(Mono.empty());

        //Act
        var service = this.service.deletePlaylist("1618");

        //Assert
        StepVerifier.create(service).expectNext(responseDelete).expectComplete().verify();
        Mockito.verify(playlistRepository).findById("1618");
        Mockito.verify(playlistRepository).deleteById("1618");
    }
}