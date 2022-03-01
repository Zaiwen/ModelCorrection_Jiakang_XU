package au.com.d2dcrc.yago2es;

import org.elasticsearch.common.xcontent.support.XContentMapValues;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Merges JSON-LD documents
 */
// Implements only "just-enough" handling for JSON-LD to work with our YAGO importer. Ideally, this would also
// understand enough about namespacing, types, and JSON-LD normalisation to be generically useful.
public class JsonLdUpdater {

    /**
     * An initial document to use when there are changes to merge, but no existing document to merge into.
     *
     * @return A template JSON-LD document.
     */
    public static Map<String, Object> initial() {
        return new HashMap<>();
    }

    /**
     * Updates an existing document by merging in the values from another document.
     *
     * @param target The document to update.
     * @param updates The document containing the changes to merge.
     * @return The number of properties that were changed.
     */
    public int update(final Map<String, Object> target, final Map<String, Object> updates) {

        int count = 0;
        for (Map.Entry<String, Object> updateEntry : updates.entrySet()) {
            final String path = updateEntry.getKey();
            final Object previousNode = target.get(path);
            final Object newNode = updates.get(path);

            if (previousNode != null) {
                if (!previousNode.equals(newNode)) {
                    if (XContentMapValues.isObject(previousNode)) {
                        if (XContentMapValues.isObject(newNode)) {

                            // Recursively update objects, but not if they are identifiable objects or value objects.

                            final Map<String, Object> previousObject =
                                XContentMapValues.nodeMapValue(previousNode, "previous");
                            final Map<String, Object> newObject = XContentMapValues.nodeMapValue(newNode, "new");

                            final boolean previousHasId = previousObject.containsKey("@id");
                            final boolean newHasId = newObject.containsKey("@id");
                            final boolean equalIds = Objects.equals(previousObject.get("@id"), newObject.get("@id"));
                            final boolean newHasValue = newObject.containsKey("@value");
                            final boolean previousHasValue = previousObject.containsKey("@value");
                            final boolean equalValueStrings =
                                Objects.equals(newObject.get("@value"), newObject.get("@value"));

                            if ((!previousHasId && !newHasId && !newHasValue && !previousHasValue) ||
                                previousHasId && equalIds || newHasId && equalIds ||
                                previousHasValue && equalValueStrings || newHasValue && equalValueStrings) {
                                count += update(previousObject, newObject);
                            } else {
                                final List<Object> newList = new ArrayList<>(2);
                                newList.add(previousNode);
                                newList.add(newNode);
                                target.put(path, newList);
                                count++;
                            }
                        } else {
                            final List<Object> newList = new ArrayList<>(2);
                            newList.add(previousNode);
                            newList.add(newNode);
                            target.put(path, newList);
                            count++;
                        }
                    } else if (XContentMapValues.isArray(previousNode)) {
                        @SuppressWarnings("unchecked")
                        final List<Object> destinationNode = (List<Object>) previousNode;
                        if (XContentMapValues.isArray(newNode)) {
                            @SuppressWarnings("unchecked")
                            final List<Object> items = (List<Object>) newNode;
                            for (Object item : items) {
                                if (!destinationNode.contains(item)) {
                                    destinationNode.add(item);
                                    count++;
                                }
                            }
                        } else {
                            if (!destinationNode.contains(newNode)) {
                                destinationNode.add(newNode);
                                count++;
                            }
                        }
                    } else {
                        final List<Object> newList = new ArrayList<>(2);
                        newList.add(previousNode);
                        newList.add(newNode);
                        target.put(path, newList);
                        count++;
                    }
                }
            } else {
                target.put(path, newNode);
                count++;
            }
        }

        return count;
    }
}
