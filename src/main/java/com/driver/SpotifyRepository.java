package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository() {
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();

    }


    public User createUser(String name, String mobile) {
        if (name == null || mobile == null) {
            return null;
        }
        User newUser = new User(name, mobile);
        users.add(newUser);

        userPlaylistMap.put(newUser, new ArrayList<>());
        return newUser;
    }

    public Artist createArtist(String name) {
        Artist newArtist = new Artist(name);
        artists.add(newArtist);
        return newArtist;
    }

    public Album createAlbum(String title, String artistName) {
        Artist artist = findArtistByName(artistName);

        if (artist == null) {
            artist = createArtist(artistName);
        }

        Album album = new Album(title);
        albums.add(album);
        artistAlbumMap.get(artist).add(album);
        albumSongMap.put(album, new ArrayList<>());
        return album;
    }


    public Song createSong(String title, String albumName, int length) throws Exception {
        Album album = getTheAlbumFromName(albumName);

        if (album == null) {
            throw new Exception("Album does not exist");
        }

        Song song = new Song(title, length);
        song.setLikes(0);
        songs.add(song);

        albumSongMap.get(album).add(song);
        songLikeMap.put(song, new ArrayList<>());

        return song;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        User user = findUserByMobile(mobile);

        if (user == null) {
            throw new Exception("User Not Found");
        }

        Playlist playlist = new Playlist(title);
        playlists.add(playlist);

        playlistSongMap.put(playlist,new ArrayList<>());
        playlistListenerMap.put(playlist,new ArrayList<>());

        for (Song song : songs) {
            if (song.getLength() == length) {
                playlistSongMap.get(playlist).add(song);
            }
        }

        for(Song song:songs){
            if(song.getLength() == length)
                playlistSongMap.get(playlist).add(song);
        }

        playlistListenerMap.get(playlist).add(user);
        creatorPlaylistMap.put(user,playlist);
        userPlaylistMap.get(user).add(playlist);


        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user = findUserByMobile(mobile);

        if(user == null)
            throw new Exception("User does not exist");

        Playlist playlist=new Playlist(title);
        playlists.add(playlist);

        playlistSongMap.put(playlist,new ArrayList<>());
        playlistListenerMap.put(playlist,new ArrayList<>());

        for(Song song:songs){
            if(songTitles.contains(song.getTitle()))
                playlistSongMap.get(playlist).add(song);
        }

        playlistListenerMap.get(playlist).add(user);
        creatorPlaylistMap.put(user,playlist);
        userPlaylistMap.get(user).add(playlist);


        return playlist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        User user = findUserByMobile(mobile);

        if (user == null) {
            throw new Exception("User Not Found");
        }

        Playlist playlistToFind = null;

        for (Playlist playlist : playlists) {
            if (playlist.getTitle().equals(playlistTitle)) {
                playlistToFind = playlist;
                break;
            }
        }

        if (playlistToFind == null) {
            throw new Exception("Playlist does not exist");
        }


        if(creatorPlaylistMap.containsKey(user) && creatorPlaylistMap.get(user) == playlistToFind ||
                playlistListenerMap.get(playlistToFind).contains(user)){

            return playlistToFind;
        }

        playlistListenerMap.get(playlistToFind).add(user);

        if(!userPlaylistMap.get(user).contains(playlistToFind)){
            userPlaylistMap.get(user).add(playlistToFind);
        }

        return playlistToFind;
    }


    public Song likeSong(String mobile, String songTitle) throws Exception {
        // Find the user
        User user = findUserByMobile(mobile);
        if (user == null) {
            throw new Exception("User not found");
        }

        // Find the song
        Song song = findSongByTitle(songTitle);
        if (song == null) {
            throw new Exception("Song not found");
        }

        // Check if the user has already liked the song
        if (songLikeMap.get(song).contains(user)) {
            return song;
        }

        // Like the song
        song.setLikes(song.getLikes()+1);
        songLikeMap.get(song).add(user);

        for(Album album:albumSongMap.keySet()){
            if(albumSongMap.get(album).contains(song)){
                for(Artist artist:artistAlbumMap.keySet()){
                    if(artistAlbumMap.get(artist).contains(album)){
                        artist.setLikes(artist.getLikes()+1);
                        break;
                    }
                }
                break;
            }
        }
        return song;

    }

    public String mostPopularArtist() {
        int countLikes=Integer.MIN_VALUE;
        String popularArtist="";
        for(Artist artist:artists){
            if(artist.getLikes() > countLikes){
                popularArtist=artist.getName();
                countLikes=artist.getLikes();
            }
        }
        return popularArtist;
    }

    public String mostPopularSong() {
        int countLikes=Integer.MIN_VALUE;
        String popularSong="";
        for(Song song:songs){
            if(song.getLikes() > countLikes){
                popularSong=song.getTitle();
                countLikes=song.getLikes();
            }
        }
        return popularSong;
    }



    //    additional methods
    public Artist findArtistByName(String name) {
        // Loop through the existing artists to find the one with the given name
        for (Artist artist : artists) {
            if (artist.getName().equals(name)) {
                return artist;
            }
        }
        return null; // Return null if artist not found
    }


    private Album getTheAlbumFromName(String albumName) {
        for (Album album : albums) {
            if (album.getTitle().equals(albumName)) {
                return album;
            }
        }

        return null;
    }


    public User findUserByMobile(String mobileNumber) {
        for (User user : users) {
            if (user.getMobile().equals(mobileNumber)) {
                return user;
            }
        }

        return null;
    }

    private Song findSongByTitle(String title) {
        for (Song song : songs) {
            if (song.getTitle().equals(title)) {
                return song;
            }
        }
        return null;
    }

}
