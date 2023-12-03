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
        System.out.println("User List: ");
        display(User.class, users);
        return newUser;
    }

    public Artist createArtist(String name) {
        Artist newArtist = new Artist(name);
        artists.add(newArtist);
        System.out.println("Artist List: ");
        display(Artist.class, artists);
        return newArtist;
    }

    public Album createAlbum(String title, String artistName) {
        Album album = new Album(title);
        Artist artist = findArtistByName(artistName);

        if (artist != null && artistAlbumMap != null) {
            List<Album> albumList = artistAlbumMap.get(artist);

            if (albumList == null) {
                albumList = new ArrayList<>();
            }

            albumList.add(album);
            artistAlbumMap.put(artist, albumList);
        } else {
            artist = new Artist(artistName);
            artists.add(artist);
            List<Album> albumList = new ArrayList<>();
            albumList.add(album);
            artistAlbumMap.put(artist, albumList);
        }

        albums.add(album);
        System.out.println("Albums List: ");
        display(Album.class, albums);
        return album;
    }


    public Song createSong(String title, String albumName, int length) throws Exception {
        Album album = getTheAlbumFromName(albumName);
        Song song;
        List<Song> songList;
        if (album != null && albumSongMap != null) {
            song = new Song(title, length);
            songs.add(song);
            songList = albumSongMap.get(album);
            albumSongMap.put(album, songList);
            System.out.println("Song List : ");
            display(Song.class, songs);
            return song;
        } else {
            throw new Exception("Album does not exist");
        }
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        User playlistCreator = findUserByMobile(mobile);
        if (playlistCreator == null) {
            throw new Exception("User Not Found");
        }

        Playlist playlist = new Playlist(title);
        List<Song> selectedSong = new ArrayList<>();
        for (Song song : songs) {
            if (song.getLength() == length) {
                selectedSong.add(song);
            }
        }

        if (selectedSong.isEmpty()) {
            throw new Exception("No songs found with the specified length");
        }

        playlistSongMap.put(playlist, selectedSong);
        playlists.add(playlist);

        List<User> listeners = new ArrayList<>();
        listeners.add(playlistCreator);
        playlistListenerMap.put(playlist, listeners);

        // Update the user's playlist map
        List<Playlist> userPlaylists = userPlaylistMap.getOrDefault(playlistCreator, new ArrayList<>());
        userPlaylists.add(playlist);
        userPlaylistMap.put(playlistCreator, userPlaylists);

        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User playlistCreator = findUserByMobile(mobile);

        if (playlistCreator == null) {
            throw new Exception("User Not Found");
        }

        Playlist playlist = new Playlist(title);
        List<Song> selectedSongs = new ArrayList<>();

        for (String songTitle : songTitles) {
            Song song = findSongByTitle(songTitle);
            if (song != null) {
                selectedSongs.add(song);
            } else {
                throw new Exception("Song with title " + songTitle + " not found");
            }
        }

        if (selectedSongs.isEmpty()) {
            throw new Exception("No songs found with the specified titles");
        }

        playlistSongMap.put(playlist, selectedSongs);
        playlists.add(playlist);

        List<User> listeners = new ArrayList<>();
        listeners.add(playlistCreator);
        playlistListenerMap.put(playlist, listeners);

        // Update the user's playlist map
        List<Playlist> userPlaylists = userPlaylistMap.getOrDefault(playlistCreator, new ArrayList<>());
        userPlaylists.add(playlist);
        userPlaylistMap.put(playlistCreator, userPlaylists);

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
            throw new Exception("Playlist with title " + playlistTitle + " not found");
        }

        // Check if the user is the creator or already a listener
        if (!playlistListenerMap.containsKey(playlistToFind) ||
                (creatorPlaylistMap.containsKey(playlistToFind) && creatorPlaylistMap.get(playlistToFind).equals(user)) ||
                playlistListenerMap.get(playlistToFind).contains(user)) {
            // If the user is the creator or already a listener, do nothing
            return playlistToFind;
        } else {
            // Add the user as a listener
            List<User> listeners = playlistListenerMap.getOrDefault(playlistToFind, new ArrayList<>());
            listeners.add(user);
            playlistListenerMap.put(playlistToFind, listeners);

            // Update the user's playlist map
            List<Playlist> userPlaylists = userPlaylistMap.getOrDefault(user, new ArrayList<>());
            userPlaylists.add(playlistToFind);
            userPlaylistMap.put(user, userPlaylists);


            return playlistToFind;
        }
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
        if (songLikeMap.containsKey(song) && songLikeMap.get(song).contains(user)) {
            System.out.println("User already liked the song");
            return song;
        }

        // Like the song
        List<User> likedUsers = songLikeMap.getOrDefault(song, new ArrayList<>());
        likedUsers.add(user);
        songLikeMap.put(song, likedUsers);

        // Auto-like the corresponding artist(s)
        List<Album> albumsWithSong = getAlbumsContainingSong(song);
        for (Album album : albumsWithSong) {
            Artist artist = getArtistFromAlbum(album);
            artist.setLikes(artist.getLikes()+1);
        }

        return song;
    }



    public String mostPopularArtist() {
        int maxLikes = 0;
        Artist mostPopularArtist = null;

        for (Map.Entry<Artist, List<Album>> entry : artistAlbumMap.entrySet()) {
            Artist artist = entry.getKey();
            int artistLikes = getLikesForEntity(artist);

            if (artistLikes > maxLikes) {
                maxLikes = artistLikes;
                mostPopularArtist = artist;
            }
        }

        return mostPopularArtist != null ? mostPopularArtist.getName() : "No artists found";
    }

    public String mostPopularSong() {
        int maxLikes = 0;
        Song mostPopularSong = null;

        for (Song song : songs) {
            int songLikes = getLikesForEntity(song);

            if (songLikes > maxLikes) {
                maxLikes = songLikes;
                mostPopularSong = song;
            }
        }

        return mostPopularSong != null ? mostPopularSong.getTitle() : "No songs found";
    }

    private int getLikesForEntity(Object entity) {
        List<User> likedUsers = songLikeMap.getOrDefault(entity, new ArrayList<>());
        return likedUsers.size();
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


    public <T> void display(Class<T> tClass, List<T> list) {
        for (T t : list) {
            System.out.println(t.toString());
        }
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

    private List<Album> getAlbumsContainingSong(Song song) {
        List<Album> albumsWithSong = new ArrayList<>();

        for (Map.Entry<Album, List<Song>> entry : albumSongMap.entrySet()) {
            if (entry.getValue().contains(song)) {
                albumsWithSong.add(entry.getKey());
            }
        }

        return albumsWithSong;
    }

    private Artist getArtistFromAlbum(Album album) {
        for (Map.Entry<Artist, List<Album>> entry : artistAlbumMap.entrySet()) {
            if (entry.getValue().contains(album)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
