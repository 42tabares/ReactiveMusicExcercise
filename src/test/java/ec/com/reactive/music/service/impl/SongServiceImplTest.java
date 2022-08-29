package ec.com.reactive.music.service.impl;

import ec.com.reactive.music.domain.dto.AlbumDTO;
import ec.com.reactive.music.domain.dto.SongDTO;
import ec.com.reactive.music.domain.entities.Album;
import ec.com.reactive.music.domain.entities.Song;
import ec.com.reactive.music.repository.IAlbumRepository;
import ec.com.reactive.music.repository.ISongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
class SongServiceImplTest {

    @Mock
    ISongRepository songRepository;

    ModelMapper modelMapper;

    SongServiceImpl service;

    @BeforeEach
    void init(){
        modelMapper = new ModelMapper();
        service = new SongServiceImpl(songRepository,modelMapper);
    }

    @Test
    @DisplayName("findAllSongs")
    void findAllSongs(){

        //Arrange
        ArrayList<Song> expectedSongs = new ArrayList<>();
        expectedSongs.add(new Song());
        expectedSongs.add(new Song());
        ArrayList<SongDTO> songsDTO = expectedSongs.stream().map(song -> modelMapper.map(song,SongDTO.class)).collect(Collectors.toCollection(ArrayList::new));

        var fluxResult = Flux.fromIterable(expectedSongs);
        var fluxResultDTO = Flux.fromIterable(songsDTO);
        ResponseEntity<Flux<SongDTO>> responseEntity = new ResponseEntity<>(fluxResultDTO, HttpStatus.FOUND);
        Mockito.when(songRepository.findAll()).thenReturn(fluxResult);

        //Act
        var serviceCommand = service.findAllSongs();

        //Assert
        StepVerifier.create(serviceCommand)
                .expectNextMatches(fluxResponseEntity -> fluxResponseEntity.getStatusCode().is3xxRedirection())
                .expectComplete().verify();
    }

    //No Content Scenario
    @Test
    void findAllSongsError() {

        //Arrange
        ResponseEntity<SongDTO> expectedResponse = new ResponseEntity<>(HttpStatus.NO_CONTENT);
        Mockito.when(songRepository.findAll()).thenReturn(Flux.empty());

        //Act
        var serviceCommand = service.findAllSongs();

        //Assert
        StepVerifier.create(serviceCommand)
                .expectNextMatches(fluxResponseEntity -> fluxResponseEntity.getStatusCode()
                        .equals(expectedResponse.getStatusCode()))
                        .expectComplete().verify();
    }

    @Test
    void findSongById() {
        //Arrange
        Song songExpected = new Song();
        songExpected.setIdSong("31416");
        songExpected.setIdAlbum("1618");
        songExpected.setName("Lorem Ipsum");

        var songDTOExpected = modelMapper.map(songExpected,SongDTO.class);

        ResponseEntity<SongDTO> songDTOResponse = new ResponseEntity<>(songDTOExpected,HttpStatus.FOUND);

        Mockito.when(songRepository.findById(Mockito.any(String.class))).thenReturn(Mono.just(songExpected));

        //Act
        var serviceCommand = service.findSongById("31416");

        //Assert
        StepVerifier.create(serviceCommand)
                .expectNext(songDTOResponse)
                .expectComplete()
                .verify();
        Mockito.verify(songRepository).findById("31416");
    }

    @Test
    void findSongByIdError(){
        //Arrange
        Song songExpected = new Song();
        songExpected.setIdSong("31416");
        songExpected.setIdAlbum("1618");
        songExpected.setName("Lorem Ipsum");

        ResponseEntity<SongDTO> songDTOResponseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        Mockito.when(songRepository.findById(Mockito.any(String.class))).thenReturn(Mono.empty());

        //Act
        var serviceCommand = service.findSongById("-");

        //Assert
        StepVerifier.create(serviceCommand)
                .expectNext(songDTOResponseEntity)
                .expectComplete().verify();
        Mockito.verify(songRepository).findById(Mockito.any(String.class));
    }

    @Test
    void saveSong() {
        //Arrange
        Song songExpected = new Song();
        songExpected.setIdSong("31416");
        songExpected.setIdAlbum("1618");
        songExpected.setName("Lorem Ipsum");

        SongDTO songExpectedDTO = modelMapper.map(songExpected, SongDTO.class);
        ResponseEntity<SongDTO> songDTOResponseEntity = new ResponseEntity<>(songExpectedDTO, HttpStatus.CREATED);

        Mockito.when(songRepository.save(Mockito.any(Song.class))).thenReturn(Mono.just(songExpected));

        //Act
        var serviceCommand = service.saveSong(songExpectedDTO);

        //Assert
        StepVerifier.create(serviceCommand)
                .expectNext(songDTOResponseEntity)
                .expectComplete()
                .verify();

        Mockito.verify(songRepository).save(songExpected);
    }

    @Test
    void saveSongError(){
        //Arrange
        Song songExpected = new Song();
        songExpected.setIdSong("31416");
        songExpected.setIdAlbum("1618");
        songExpected.setName("Lorem Ipsum");

        SongDTO songDTO = modelMapper.map(songExpected,SongDTO.class);

        ResponseEntity<SongDTO> songDTOResponseEntity = new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
        Mockito.when(songRepository.save(Mockito.any(Song.class))).thenReturn(Mono.empty());

        //Act
        var serviceCommand = service.saveSong(songDTO);

        //Assert
        StepVerifier.create(serviceCommand)
                .expectNext(songDTOResponseEntity)
                .expectComplete().verify();
        Mockito.verify(songRepository).save(Mockito.any(Song.class));
    }

    @Test
    void deleteSong() {
        //Arrange
        Song songExpected = new Song();
        songExpected.setIdSong("31416");
        songExpected.setIdAlbum("1618");
        songExpected.setName("Lorem Ipsum");

        ResponseEntity<String> responseEntity = new ResponseEntity<>(songExpected.getIdSong(),HttpStatus.ACCEPTED);
        Mockito.when(songRepository.findById(Mockito.any(String.class))).thenReturn(Mono.just(songExpected));
        Mockito.when(songRepository.deleteById(Mockito.any(String.class))).thenReturn(Mono.empty());

        //Act
        var songService = service.deleteSong("31416");

        //Assert
        StepVerifier.create(songService).expectNext(responseEntity).expectComplete().verify();
        Mockito.verify(songRepository).findById(Mockito.any(String.class));
        Mockito.verify(songRepository).deleteById(Mockito.any(String.class));
    }

    @Test
    void deleteSongError(){

        //Arrange
        ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        Mockito.when(songRepository.findById(Mockito.any(String.class))).thenReturn(Mono.empty());

        //Act
        var serviceCommand = service.deleteSong("-");

        //Assert
        StepVerifier.create(serviceCommand).expectNext(responseEntity).expectComplete().verify();
        Mockito.verify(songRepository).findById(Mockito.any(String.class));
    }
}