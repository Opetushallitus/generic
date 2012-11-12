/*
 *
 * Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * European Union Public Licence for more details.
 */

package fi.vm.sade.generic.ui.component;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.*;

import fi.vm.sade.generic.common.ClassUtils;
import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.ui.StyleNames;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Searchbox + tree. Tree shows items, and search allows to filter the items visible in the tree.
 * NOTES:
 *  - after creation, init- and reload-method must be called
 *  - init-method creates inner components
 *  - reload-method loads items, and creates tree hierarchy out of them with the help of TreeAdapter
 *  - searchableTree's inner components can be modified after initializing tree
 *  - for example clickListener can be set like this: searchableTree.getTree().addListener(new ItemClickEvent.ItemClickListener(){...
 *
 * @author Antti Salonen
 */
@Configurable(preConstruction = false)
public class SearchableTree<T> extends VerticalLayout {

    private static final long serialVersionUID = 937592759375875L;

    private static final Logger LOG = LoggerFactory.getLogger(SearchableTree.class);
    protected HierarchicalContainer dataSource = new HierarchicalContainer();

    @DebugId(id = "org_tree") // TODO: rename debugIds, not organisaatio specific anymore..
    protected Tree tree = new Tree(I18N.getMessage("Hakupuu.lblPuu"), dataSource); // TOD: remove captions from here, can be set afterwards outside SearchableTree

    @DebugId(id = "org_tree_search")
    TextField searchBox = new TextField(I18N.getMessage("Hakupuu.lblHaku"));

    // add tabpanel
    TabSheet tabSheet = new TabSheet();


    @DebugId(id = "org_resultcount")
    private Label labelResultCount = new Label();

    private String debugIdPrefix;
    protected TreeAdapter<T> treeAdapter;

    protected VerticalLayout basicSearch;

    public SearchableTree(String debugIdPrefix, TreeAdapter<T> treeAdapter) {
        this.debugIdPrefix = debugIdPrefix;
        this.treeAdapter = treeAdapter;
        this.setDebugId(debugIdPrefix+"searchableTree");
    }

    protected void hideTab() {
        this.removeComponent(tabSheet);
        this.addComponent(basicSearch);
    }

    public void init() {
        
        dataSource.addContainerProperty("caption", String.class, "");
        tree.setItemCaptionPropertyId("caption");
        searchBox.setInputPrompt("Kirjoita osa organisaation nimestä tai y-tunnuksesta");
        searchBox.setStyleName("search");
        searchBox.addListener(new FieldEvents.TextChangeListener() {
            public void textChange(final FieldEvents.TextChangeEvent event) {
                searchTextChanged(event.getText());
            }
        });


        this.addComponent(tabSheet);
        basicSearch = new VerticalLayout();
        basicSearch.addStyleName(StyleNames.GRID_16);
        tabSheet.addTab(basicSearch, I18N.getMessage("Hakupuu.lblTab"));

        // add search box
        searchBox.setWidth("23em"); // TODO: 100% ei toimi tässä, customlayout?
        basicSearch.addComponent(searchBox);

        // add "X tulosta" label
        basicSearch.addComponent(labelResultCount);

        // add tree
        Panel panel = new Panel();
        panel.addComponent(tree);
        basicSearch.addComponent(panel);

        processDebugIds(debugIdPrefix);
    }

    protected void searchTextChanged(final String searchText) {
        // apply search terms to tree item filter
        dataSource.removeAllContainerFilters();
        if (searchText.length() > 0) {
            SimpleStringFilter filter = new SimpleStringFilter("caption", searchText, true, false);
            dataSource.addContainerFilter(filter);
        }

        // expand all visible items
        Collection visibleItems = tree.getVisibleItemIds();
        for (Object visibleItem : visibleItems) {
            tree.expandItemsRecursively(visibleItem);
        }

        // highlight found items
        tree.setItemStyleGenerator(new Tree.ItemStyleGenerator() {
            public String getStyle(Object itemId) {
                if (itemCaptionMatchesSearch(searchText, itemId)) {
                    return "highlight";
                }
                return null;
            }
        });

        // count results (ei voi tehdä filtteröinnin ytheydessä, serveriltä ei voi päivittää (filter)säikeestä komponenttia)
        setResultCount(countResults(searchText));
    }

    public int countResults(String searchText) {
        int count = 0;
        for (Object itemId : tree.getVisibleItemIds()) {
            if (itemCaptionMatchesSearch(searchText, itemId)) {
                count++;
            }
        }
        return count;
    }

    protected boolean itemCaptionMatchesSearch(String searchText, Object itemId) {
        String caption = treeAdapter.getCaption((T) itemId);
        return searchText.length() > 0 && caption.toLowerCase().contains(searchText.toLowerCase());
    }

    protected void setResultCount(int count) {
        labelResultCount.setCaption("" + count + I18N.getMessage("Hakupuu.lblTulosta"));
    }

    public void reload() {
        // clear searchbox
        searchBox.setValue("");
        searchTextChanged("");
        // clear data
        dataSource.removeAllItems();
        // load new data
        Collection<T> all = treeAdapter.findAll();
        // add data to tree
        addTreeNodes(all);
        // set result count
        setResultCount(all.size());
    }

    protected void addTreeNodes(Collection<T> all) {
        // add nodes
        Map<Object,Object> nodesById = new HashMap<Object, Object>();
        for (T node : all) {
            Object id = treeAdapter.getId(node);
            String caption = treeAdapter.getCaption(node);
            Item item = dataSource.addItem(node);
            //LOG.info(" ########## addTreeNodes, id: "+id+", caption: "+caption+", item: "+item);
            Property captionProperty = item.getItemProperty("caption");
            captionProperty.setValue(caption);
            nodesById.put(id, node);
            dataSource.setChildrenAllowed(node, false); // will be set true for parents later
        }
        // set parent-child relationships
        for (T node : all) {
            Object parentId = treeAdapter.getParentId(node);
            //DEBUGSAWAY:LOG.debug("    tree.reload, id: " + treeAdapter.getId(node) + ", parentId: " + parentId + ", caption: " + treeAdapter.getCaption(node));
            if (parentId != null) {
                //DEBUGSAWAY:LOG.debug("    tree, set child->parent: "+treeAdapter.getId(node)+"->"+parentId);
                Object parent = nodesById.get(parentId);
                dataSource.setChildrenAllowed(parent, true);
                dataSource.setParent(node, parent);
            }
        }
    }

    public Tree getTree() {
        return tree;
    }

    public TextField getSearchBox() {
        return searchBox;
    }

    private void processDebugIds(String debugIdPrefix) {
        if (debugIdPrefix == null) {
            debugIdPrefix = "";
        }
        List<Field> fields = ClassUtils.getDeclaredFields(getClass());
        for (Field field : fields) {
            DebugId debugId = field.getAnnotation(DebugId.class);
            if (debugId != null) {
                field.setAccessible(true);
                try {
                    Component component = (Component) field.get(this);
                    String id = debugIdPrefix + debugId.id();
                    component.setDebugId(id);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
