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
    static public <E> void saveFavourites(String key, DefaultListModel<E> favouriteModel) {
        Map<String, List<E>> map = new HashMap<>();
        List<E> favouriteList = IntStream.range(0, favouriteModel.size())
                .mapToObj(favouriteModel::getElementAt)
                .collect(Collectors.toList());

        String data = CacheManager.getCachedDataAsString(cacheName);
        try {
            map = mapper.readValue(data, new TypeReference<Map<String, List<E>>>() { });
        } catch (JsonProcessingException ignored) {}

        map.put(key, favouriteList);

        try {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);

            data = mapper.writerWithDefaultPrettyPrinter().with(prettyPrinter).writeValueAsString(map);
            CacheManager.saveDataAsCache(cacheName, data);
        } catch (JsonProcessingException ignored) {}
    }

    static public <E> List<E> loadFavourites(String key, Class<E> valueType) {
        List<E> favourites = new ArrayList<>();
        String data = CacheManager.getCachedDataAsString(cacheName);

        try {
            Map<String, List<E>> map = mapper.readValue(data, new TypeReference<Map<String, List<E>>>() { });
            List<E> favouriteList = map.get(key);
            if (favouriteList != null) {
                favourites = favouriteList.stream()
                        .map(item -> mapper.convertValue(item, valueType))
                        .collect(Collectors.toList());
            }
        } catch (JsonProcessingException ignored) {}

        return favourites;
    }

    static ObjectMapper mapper = new ObjectMapper();
    static DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
    static String cacheName = "../favourite.json";
}
