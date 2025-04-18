package org.ev3nt.files;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FavouriteManager {
    static public void saveFavourites(String key, DefaultListModel<String> favouriteModel) {
        Map<String, List<String>> map = new HashMap<>();
        List<String> favouriteList = IntStream.range(0, favouriteModel.size())
                .mapToObj(favouriteModel::getElementAt)
                .collect(Collectors.toList());

        map.put(key, favouriteList);

        try {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);

            String data = mapper.writerWithDefaultPrettyPrinter().with(prettyPrinter).writeValueAsString(map);
            CacheManager.saveDataAsCache(cacheName, data);
        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
        }
    }

    static public List<String> loadFavourites(String key) {
        List<String> favourites = new ArrayList<>();
        String data = CacheManager.getCachedDataAsString(cacheName);

        try {
            Map<String, List<String>> map = mapper.readValue(data, new TypeReference<Map<String, List<String>>>() { });
            favourites.addAll(map.get(key));
        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
        }

        return favourites;
    }

    static ObjectMapper mapper = new ObjectMapper();
    static DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
    static String cacheName = "../favourite.json";
}
