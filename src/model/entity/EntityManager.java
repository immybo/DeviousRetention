package model.entity;

import model.Player;

import java.io.Serializable;
import java.util.*;

public class EntityManager implements Serializable {
    private final Map<String, EntityTemplate> allTemplates;
    private Map<Player, Map<String, EntityTemplate>> entityTemplates;
    private final String[] defaultTemplateNames;

    /**
     * An entity manager manages the available entity templates for each player.
     * By default, an entity manager tracks no players. Call "addPlayer" to add
     * a player to track.
     *
     * @param allTemplates The master list of templates; any enabled templates must be present in this.
     * @param defaultTemplateNames The templates which are enabled by default for a new player if nothing else is specified.
     */
    public EntityManager(EntityTemplate[] allTemplates, String[] defaultTemplateNames) {
        this.allTemplates = new HashMap<String, EntityTemplate>();
        for (EntityTemplate template : allTemplates) {
            // Template names can't change, so it's fine to use it as the unique key
            this.allTemplates.put(template.getName(), template);
        }

        this.entityTemplates = new HashMap<>();
        this.defaultTemplateNames = defaultTemplateNames;
    }

    /**
     * Adds a new player with only the given list of names enabled.
     */
    public void addPlayer(Player p, String[] enabled) {
        entityTemplates.put(p, new HashMap<>());
        enableTemplates(p, enabled);
    }

    /**
     * Adds a new player with only the default templates enabled.
     * If there are no default templates, enables nothing for the new player.
     */
    public void addPlayer(Player p) {
        addPlayer(p, defaultTemplateNames);
    }

    /**
     * Enables the given templates for the given player.
     * @throws IllegalStateException If one of the templates was not found.
     */
    public void enableTemplates(Player p, String[] templateNames) throws IllegalStateException {
        for (String templateName : templateNames) {
            enableTemplate(p, templateName);
        }
    }

    /**
     * Enables the given template for the given player.
     * @throws IllegalStateException If the template was not found.
     */
    public void enableTemplate(Player p, String templateName) throws IllegalStateException {
        EntityTemplate template = getEntityTemplateByName(templateName); // Make sure that the entity template exists
        entityTemplates.get(p).put(template.getName(), template);
    }

    /**
     * Returns the entity template with the given name from the master list.
     * This does not guarantee that any particular player can create this entity.
     *
     * @throws IllegalStateException If no entity by the given name was found.
     */
    public EntityTemplate getEntityTemplateByName(String templateName) throws IllegalStateException {
        EntityTemplate template = allTemplates.get(templateName);
        if (template == null) {
            throw new IllegalStateException("EntityTemplate by name " + templateName + " not found.");
        }
        return template;
    }

    /**
     * Returns whether or not the entity template with the given name is enabled
     * for the given player; i.e. whether or not they can create it.
     *
     * @throws IllegalStateException If no entity by the given name was found.
     */
    public boolean isEnabled(Player p, String templateName) throws IllegalStateException {
        return entityTemplates.get(p).containsKey(templateName);
    }

    /**
     * Returns a reference to the template map for the given player.
     * This can be updated, and it will update those templates for the given player.
     */
    public Map<String, EntityTemplate> getTemplatesForPlayer(Player p) {
        return entityTemplates.get(p);
    }
}
