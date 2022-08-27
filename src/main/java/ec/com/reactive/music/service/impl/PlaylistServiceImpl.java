package ec.com.reactive.music.service.impl;

import ec.com.reactive.music.domain.dto.AlbumDTO;
import ec.com.reactive.music.domain.dto.PlaylistDTO;
import ec.com.reactive.music.domain.dto.SongDTO;
import ec.com.reactive.music.domain.entities.Album;
import ec.com.reactive.music.domain.entities.Playlist;
import ec.com.reactive.music.domain.entities.Song;
import ec.com.reactive.music.repository.IAlbumRepository;
import ec.com.reactive.music.repository.IPlaylistRepository;
import ec.com.reactive.music.repository.ISongRepository;
import ec.com.reactive.music.service.IAlbumService;
import ec.com.reactive.music.service.IPlaylistService;
import ec.com.reactive.music.service.ISongService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PlaylistServiceImpl implements IPlaylistService {

    @Autowired
    private IPlaylistRepository iPlaylistRepository;

    @Autowired
    private ISongRepository iSongRepository;

    @Autowired
    private ModelMapper modelMapper;


    //Services
    @Override
    public Mono<ResponseEntity<Flux<PlaylistDTO>>> findAllPlaylists() {
        return this.iPlaylistRepository
                .findAll()
                .switchIfEmpty(Mono.error(new Throwable(HttpStatus.NO_CONTENT.toString())))
                .map(this::playlistToDTO)
                .collectList()
                .map(playlistDTOS ->  new ResponseEntity<>(Flux.fromIterable(playlistDTOS),HttpStatus.FOUND))
                .onErrorResume(throwable -> Mono.just(new ResponseEntity<>(Flux.empty(),HttpStatus.NO_CONTENT)));
    }

    @Override
    public Mono<ResponseEntity<PlaylistDTO>> findPlaylistById(String id) {
        return this.iPlaylistRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new Throwable(HttpStatus.NOT_FOUND.toString())))
                .map(this::playlistToDTO)
                .map(pDTO -> new ResponseEntity<>(pDTO,HttpStatus.FOUND))
                .onErrorResume(throwable -> Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }

    @Override
    public Mono<ResponseEntity<PlaylistDTO>> savePlaylist(PlaylistDTO playlistDTO) {
        return playlistDTO.getIdPlaylist().equals("Does not exist") ? Mono.just(new ResponseEntity<>(playlistDTO, HttpStatus.NOT_ACCEPTABLE))
                : this.iPlaylistRepository
                .save(DTOToPlaylist(playlistDTO))
                .switchIfEmpty(Mono.error(new Throwable(HttpStatus.EXPECTATION_FAILED.toString())))
                .map(this::playlistToDTO)
                .map(pDTO -> new ResponseEntity<>(pDTO, HttpStatus.CREATED))
                .onErrorResume(throwable -> Mono.just(new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED)));
    }

    @Override
    public Mono<ResponseEntity<PlaylistDTO>> updatePlaylist(String id, PlaylistDTO pDto) {
        return this.iPlaylistRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new Throwable(HttpStatus.NOT_FOUND.toString())))
                .flatMap(playlist -> {
                    pDto.setIdPlaylist(playlist.getIdPlaylist());
                    return this.savePlaylist(pDto);
                })
                .map(playlistDTOResponseEntity -> new ResponseEntity<>(playlistDTOResponseEntity.getBody(),HttpStatus.ACCEPTED))
                .onErrorResume(throwable -> Mono.just(new ResponseEntity<>(HttpStatus.NOT_MODIFIED)));
    }

    @Override
    public Mono<ResponseEntity<PlaylistDTO>> addPlaylistSong(String idPlaylist, SongDTO song) {
        return this.iPlaylistRepository
                .findById(idPlaylist)
                .switchIfEmpty(Mono.error(new Throwable(HttpStatus.NOT_FOUND.toString())))
                .flatMap(playlist -> {

                    var currentsongs = playlist.getSongs();
                    var songtoadd = DTOtosong(song);
                    currentsongs.add(songtoadd);

                    playlist.setSongs(currentsongs);
                    playlist.setDuration(playlist.getDuration().plusMinutes(songtoadd.getDuration().getMinute()).plusSeconds(songtoadd.getDuration().getSecond()));

                    return this.savePlaylist(playlistToDTO(playlist));

                })
                .map(playlistDTOResponseEntity -> new ResponseEntity<>(playlistDTOResponseEntity.getBody(),HttpStatus.ACCEPTED))
                .onErrorResume(throwable -> Mono.just(new ResponseEntity<>(HttpStatus.NOT_MODIFIED)));
    }

    @Override
    public Mono<ResponseEntity<PlaylistDTO>> delPlaylistSong(String idPlaylist, SongDTO song) {
        return this.iPlaylistRepository
                .findById(idPlaylist)
                .switchIfEmpty(Mono.error(new Throwable(HttpStatus.NOT_FOUND.toString())))
                .flatMap(playlist -> {

                    var currentsongs = playlist.getSongs();
                    var songtodelete = DTOtosong(song);
                    var index= currentsongs.indexOf(songtodelete);
                    currentsongs.remove(index); //Must not remove by subject!

                    playlist.setSongs(currentsongs);
                    playlist.setDuration(playlist.getDuration().minusMinutes(songtodelete.getDuration().getMinute()).minusSeconds(songtodelete.getDuration().getSecond()));

                    return this.savePlaylist(playlistToDTO(playlist));

                })
                .map(playlistDTOResponseEntity -> new ResponseEntity<>(playlistDTOResponseEntity.getBody(),HttpStatus.ACCEPTED))
                .onErrorResume(throwable -> Mono.just(new ResponseEntity<>(HttpStatus.NOT_MODIFIED)));
    }

    @Override
    public Mono<ResponseEntity<String>> deletePlaylist(String id) {
        return this.iPlaylistRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new Throwable(HttpStatus.NOT_FOUND.toString())))
                .flatMap(playlist -> this.iPlaylistRepository
                        .deleteById(playlist.getIdPlaylist())
                        .map(monoVoid -> new ResponseEntity<>(id, HttpStatus.ACCEPTED)))
                .thenReturn(new ResponseEntity<>(id, HttpStatus.ACCEPTED))
                .onErrorResume(throwable -> Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }

    //DTO Methods
    @Override
    public Playlist DTOToPlaylist(PlaylistDTO playlistDTO) {
        return this.modelMapper.map(playlistDTO,Playlist.class);
    }

    @Override
    public PlaylistDTO playlistToDTO(Playlist playlist) {
        return this.modelMapper.map(playlist,PlaylistDTO.class);
    }

    public Song DTOtosong(SongDTO song) {
        return this.modelMapper.map(song,Song.class);
    }
}
