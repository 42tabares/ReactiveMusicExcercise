package ec.com.reactive.music.web;

import ec.com.reactive.music.domain.dto.AlbumDTO;
import ec.com.reactive.music.domain.dto.PlaylistDTO;
import ec.com.reactive.music.domain.dto.SongDTO;
import ec.com.reactive.music.domain.entities.Playlist;
import ec.com.reactive.music.service.impl.PlaylistServiceImpl;
import ec.com.reactive.music.service.impl.SongServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class PlaylistResource {
    @Autowired
    private PlaylistServiceImpl playlistService;
    @Autowired
    private SongServiceImpl songService;

    @GetMapping("/findAllPlaylist")
    private Mono<ResponseEntity<Flux<PlaylistDTO>>> getPlaylist(){
        return playlistService.findAllPlaylists();
    }

    //GET
    @GetMapping("/findPlaylist/{id}")
    private Mono<ResponseEntity<PlaylistDTO>> getPlaylistById(@PathVariable String id){
        return playlistService.findPlaylistById(id);
    }

    //POST
    @PostMapping("/savePlaylist")
    private Mono<ResponseEntity<PlaylistDTO>> postPlaylist(@RequestBody PlaylistDTO pDto){
        return playlistService.savePlaylist(pDto);
    }

    //PUT
    @PutMapping("/updatePlaylist/{id}")
    private Mono<ResponseEntity<PlaylistDTO>> putPlaylist(@PathVariable String id , @RequestBody PlaylistDTO pDto){
        return playlistService.updatePlaylist(id,pDto);
    }

    //DELETE
    @DeleteMapping("/deletePlaylist/{id}")
    private Mono<ResponseEntity<String>> deletePlaylist(@PathVariable String id){
        return playlistService.deletePlaylist(id);
    }

    @PutMapping("/addPlaylistSong/{idPlaylist}/{idSong}")
    private Mono<ResponseEntity<PlaylistDTO>> putPlaylist(@PathVariable String idPlaylist ,@PathVariable String idSong){
        return songService.findSongById(idSong)
                .flatMap(songDTOResponseEntity -> songDTOResponseEntity.getStatusCode().is4xxClientError() ?
                        playlistService.addPlaylistSong("NOSONGEXISTS", new SongDTO())
                        : playlistService.addPlaylistSong(idPlaylist,songDTOResponseEntity.getBody()));
    }

    @DeleteMapping("/delPlaylistSong/{idPlaylist}/{idSong}")
    private Mono<ResponseEntity<PlaylistDTO>> deletePlaylist(@PathVariable String idPlaylist ,@PathVariable String idSong){
        return songService.findSongById(idSong)
                .flatMap(songDTOResponseEntity -> songDTOResponseEntity.getStatusCode().is4xxClientError() ?
                        playlistService.delPlaylistSong("NOSONGEXISTS", new SongDTO())
                        : playlistService.delPlaylistSong(idPlaylist,songDTOResponseEntity.getBody()));
    }
}
