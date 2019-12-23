package main.buttons;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public class Group extends Button implements Comparable<Group> {

    public static final int SIZE = 128;
    public static final int GAP = 30;
    public static final int GROUP_X = GAP;
    public static final int GROUP_Y = GAP + 20;
    private static final float GROUP_MOVING_RATE = 15;
    private static final int ALPHA_CHANGE = 20;

    public static int groupCountX;
    public static int groupCountY;
    private static int maxGroups;

    public static int totalPages;
    public static int pageNumber = 0;

    static int groupSelectedX;
    public static int groupSelectedAY;
    public static int groupSelectedBY;

    public static Group groupSelectedA;
    public static Group groupSelectedB;

    private static float deltaX;
    private static float deltaY;

    private static boolean moving = false;

    private int colour;
    private int alpha = 255;
    private int alphaChange;
    private boolean done = false;

    private String name;
    private Pack pack;

    private Group(JSONObject json, Pack pack) {
        super(SIZE, SIZE);

        this.pack = pack;
        this.name = pack.getNamespacedName(json.getString("name"));
        JSONArray colourArray = json.getJSONArray("colour");
        this.colour = main.color(colourArray.getInt(0), colourArray.getInt(1), colourArray.getInt(2));

        //check if a pack has the image, from top to bottom
        for (Pack pack1 : main.packsRoom.getLoadedPacks()) {
            if (pack1.getName().equals("Alchemy") && this.pack.getName().equals("Alchemy")) {
                //if the element is of the default pack and we are in the default pack right now, load default location
                this.setImage(main.loadImage("resources/groups/alchemy/" + this.getID() + ".png"));
                break;
            } else {
                String packPath = pack1.getPath() + "/groups/" + this.getPack().getNamespace() + "/" + this.getID() + ".png";
                if (new File(packPath).exists()) {
                    this.setImage(main.loadImage(packPath));
                    break;
                }
            }
        }
        this.getImage().resize(SIZE, SIZE);
    }

    //copy constructor
    private Group(String name, PImage image, Pack pack, int colour, int alpha, int alphaChange, boolean done) {
        super(SIZE, SIZE, image);

        this.name = name;
        this.pack = pack;
        this.colour = colour;
        this.alpha = alpha;
        this.alphaChange = alphaChange;
        this.done = done;
    }

    public String getName() {
        return this.name;
    }

    Pack getPack() {
        return this.pack;
    }

    String getID() {
        return this.name.split(":")[1];
    }

    //just added an extra !moving condition
    @Override
    protected boolean inBounds() {
        return main.mouseX >= this.getX() && main.mouseX <= this.getX() + SIZE && main.mouseY >= this.getY() && main.mouseY <= this.getY() + SIZE && !moving;
    }

    @Override
    public int compareTo(Group o) {
        if (!this.getID().equals(o.getID())) {
            return this.getID().compareTo(o.getID());
        } else {
            int thisPack = main.packsRoom.getLoadedPacks().indexOf(this.pack);
            int oPack = main.packsRoom.getLoadedPacks().indexOf(o.pack);
            return Integer.compare(thisPack, oPack);
        }
    }

    public static void reset() {
        pageNumber = 0;
        groupSelectedA = null;
        groupSelectedB = null;
    }

    public static void loadGroups(JSONArray array, Pack pack) {
        for (int i = 0; i < array.size(); i++) {
            JSONObject object = array.getJSONObject(i);
            if (object.hasKey("remove")) {
                if (object.getString("remove").equals("all")) {
                    main.loading.removeAllGroups();
                    main.groups.clear();
                } else {
                    Group group = getGroup(pack.getNamespacedName(object.getString("remove")));
                    if (group == null) {
                        System.err.println(pack.getNamespacedName(object.getString("remove")) + " group not found!");
                    } else {
                        main.loading.removeGroup(group);
                        main.groups.remove(group);
                    }
                }
            } else {
                main.groups.put(new Group(object, pack), new HashSet<>());
            }
            main.loading.updateProgress();
        }
    }

    public static Group getGroup(String name) {
        for (Group group : main.groups.keySet()) {
            if (group.name.equals(name)) {
                return group;
            }
        }
        return null;
    }

    public static void drawGroups() {
        int x = GROUP_X;
        int y = GROUP_Y;
        int maxX = (int) Math.round(main.screenWidth / 2F * 0.6); //maximum X value to draw the group grid

        groupSelectedX = maxX + 100;
        groupSelectedAY = y;
        groupSelectedBY = (int) Math.round(main.screenHeight * 0.44);

        int maxY = main.screenHeight - 60; //maximum Y value to draw the group grid
        //determine how many groups to draw horizontally
        groupCountX = Math.floorDiv(maxX - x, SIZE + GAP);
        //determine how many groups to draw vertically
        groupCountY = Math.floorDiv(maxY - y, SIZE + GAP);
        //number of groups on a page
        maxGroups = groupCountX * groupCountY;

        totalPages = Math.round(PApplet.ceil((float) main.game.getDiscovered().keySet().size() / maxGroups));

        ArrayList<Group> groups = getGroups();
        for (int i = 0; i < groups.size(); i++) {
            groups.get(i).draw(x, y);
            x += (SIZE + GAP);
            if ((i + 1) % groupCountX == 0) {
                x = GROUP_X;
                y += (SIZE + GAP);
            }
        }

        if (groupSelectedA != null) {
            if (groupSelectedA.getX() < groupSelectedX) {
                groupSelectedA.incrementX(deltaX / GROUP_MOVING_RATE);
                groupSelectedA.incrementY(deltaY / GROUP_MOVING_RATE);
                //having the if statement here ensures that it only gets run once
                if (groupSelectedA.getX() >= groupSelectedX) {
                    //need to set it so it doesn't go past
                    groupSelectedA.setX(groupSelectedX);
                    groupSelectedA.setY(groupSelectedAY);
                    moving = false;
                }
            }
            //needs to be constantly set due to screen resizing
            if (!moving) {
                groupSelectedA.setX(groupSelectedX);
                groupSelectedA.setY(groupSelectedAY);
            }
            groupSelectedA.draw();
            if (groupSelectedA.done) {
                groupSelectedA = null;
            }
        }

        if (groupSelectedB != null) {
            if (groupSelectedB.getX() < groupSelectedX) {
                groupSelectedB.incrementX(deltaX / GROUP_MOVING_RATE);
                groupSelectedB.incrementY(deltaY / GROUP_MOVING_RATE);
                //having the if statement here ensures that it only gets run once
                if (groupSelectedB.getX() >= groupSelectedX) {
                    groupSelectedB.setX(groupSelectedX);
                    groupSelectedB.setY(groupSelectedBY);
                    moving = false;
                }
            }
            //needs to be constantly set due to screen resizing
            if (!moving) {
                groupSelectedB.setX(groupSelectedX);
                groupSelectedB.setY(groupSelectedBY);
            }
            groupSelectedB.draw();
            if (groupSelectedB.done) {
                groupSelectedB = null;
            }
        }

        main.tint(255, 255);
    }

    @Override
    protected void drawButton() {
        this.updateAlpha();
        main.image(this.getImage(), this.getX(), this.getY());
    }

    private void updateAlpha() {
        if (this.alphaChange > 0) { //fade in
            if (this.alpha < 255) {
                this.alpha += this.alphaChange;
            }
            if (this.alpha >= 255) {
                this.alpha = 255;
            }
        } else if (this.alphaChange < 0) { //fade out
            if (this.alpha > 0) {
                this.alpha += this.alphaChange;
            }
            if (this.alpha <= 0) { //completely invisible
                this.alpha = 0;
                this.done = true;
            }
        }
        main.tint(255, this.alpha);
    }

    public static ArrayList<Group> getGroups() {
        ArrayList<Group> groupList = new ArrayList<>(main.game.getDiscovered().keySet());
        ArrayList<Group> list = new ArrayList<>(); //groups that are actually on screen
        for (int i = pageNumber * maxGroups; i < (pageNumber + 1) * maxGroups; i++) {
            if (i < groupList.size()) {
                list.add(groupList.get(i));
            }
        }
        //not a single group is drawn, page is empty, go to previous page
        if (list.size() == 0 && pageNumber > 0) {
            pageNumber--;
            return getGroups();
        }
        return list;
    }

    private boolean isSelected() {
        return this.getX() >= groupSelectedX;
    }

    int getColour() {
        return this.colour;
    }

    boolean exists() {
        for (Group group : main.game.getDiscovered().keySet()) {
            if (group.name.equals(this.name)) {
                return true;
            }
        }
        return false;
    }

    private Group deepCopy() {
        Group clone = new Group(this.name, this.getImage(), this.pack, this.colour, this.alpha, this.alphaChange, this.done);
        clone.setX(this.getX());
        clone.setY(this.getY());
        return clone;
    }

    @Override
    public void clicked() {
        if (!moving) {
            if (!this.isSelected()) {
                //the two blocks of code should be identical
                if (groupSelectedA == null) {
                    groupSelectedA = this.deepCopy();
                    moving = true;
                    deltaX = groupSelectedX - groupSelectedA.getX();
                    deltaY = groupSelectedAY - groupSelectedA.getY();
                    groupSelectedA.alpha = 0; //because by default alpha is 255
                    groupSelectedA.alphaChange = ALPHA_CHANGE;
                    Element.resetA();
                } else if (groupSelectedB == null) {
                    groupSelectedB = this.deepCopy();
                    moving = true;
                    deltaX = groupSelectedX - groupSelectedB.getX();
                    deltaY = groupSelectedBY - groupSelectedB.getY();
                    groupSelectedB.alpha = 0; //because by default alpha is 255
                    groupSelectedB.alphaChange = ALPHA_CHANGE;
                    Element.resetB();
                }
            } else {
                if (this == groupSelectedA) {
                    groupSelectedA.alphaChange = -ALPHA_CHANGE;
                    Element.hidePagesA();
                } else if (this == groupSelectedB) {
                    groupSelectedB.alphaChange = -ALPHA_CHANGE;
                    Element.hidePagesB();
                }
            }
        }
    }

    public static void setHintGroups(Group a, Group b) {
        groupSelectedA = a.deepCopy();
        groupSelectedB = b.deepCopy();
        Element.resetA();
        Element.resetB();
    }

}
