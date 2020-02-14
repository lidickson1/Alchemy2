# Packs Documentation

Packs control the content of the game.

## Pack Structure

```
+-- packs
|   +-- pack folder
|   |   +-- pack.json
|   |   +-- icon.png
|   |   +-- elements.json
|   |   +-- groups.json
|   |   +-- elements
|   |   |   +-- ...
|   |   +-- groups
|   |   |   +-- ...
|   |   +-- languages
|   |   |   +-- english.json
|   |   |   +-- ...
```

Both `pack.json` and `icon.png` must be present for Alchemy to recognize the folder as a pack.

## Pack Configuration File

The pack configuration file is the `pack.json` file.

```json
{
    "name": "Pack Name",
    "namespace": "test",
    "author": "Your Name"
}
```

The `name` will be the name of the pack that will be displayed in the packs list. The name in `namespace` is extremely important as it will be used for defining elements and textures.

## Elements

Elements are defined in the `elements.json` file, and the textures should be in the `elements` folder.

### Structure

The `elements.json` file should be an array of objects, with each object corresponding to either an element, a removal, or a random combination.

#### Element Structure

```json
{
    "name": "element_id",
    "group": "foo",
    "tags": ["bar"],
    "description": "My first element.",
    "combos": [
        {
            "first element": "a",
            "second element": "b"
        }
    ],
    "persistent": false,
    "variation": {}
}
```

The `name` and `group` attributes are required to define an element. `tags` is an array of strings which are helpful for defining combinations. The `description` is just a string that gets displayed along with the element's information (this is done by right clicking an element). `combos` is an array of combination objects which are further detailed down here.

`persistent` is a boolean that is used to mark an element to be persistent or not. An element is persistent if it doesn't get consumed after being used in a combination in puzzle mode. It has no effect in normal mode.

`variation` is an object that is used to dynamic alter the element's appearance, it is further documented [here](#variations).

#### Combinations

The combinations (combos) of an element are defined with several types of objects, with each yielding a different set of combinations.

##### Referring Elements

There are 3 ways to refer to elements:

-   directly specifying an element's ID (e.g. `element_id`)
-   specifying a group to refer to all of the elements in that group (e.g. `tag:bar`)
-   specifying a tag to refer to all of the elements with that tag (e.g. `group:foo`)

You can combine groups and tags with IDs in the same JSON array.

For example if the elements in group `foo` are: `a`, `b`, `c`, then

```json
["group:foo", "d"]
```

is the same as:

```json
["a", "b", "c", "d"]
```

The aforementioned syntax will not work if you want to refer to elements in a different pack, such as the default Alchemy pack because the game assumes you are referring to an element in the current pack. To fix this, you have to refer to elements with the corresponding namespace. For example, the `air` in the default Alchemy pack will be `alchemy:air` because `alchemy` is the namespace of the Alchemy pack. Similarly, tags can be written like this: `alchemy:tag:bar` and groups like this: `alchemy:group:foo`.

##### Normal Combination

The normal combination is when you only need two elements (ingredients) to create the element that you are defining right now. Since you must specify exactly two elements, you cannot use groups or tags here as they will not be parsed.

```json
{
    "first element": "a",
    "second element": "b"
}
```

This is equivalent to:

-   a + b

##### Multiple Combination

The multiple combination is when you need more than two ingredients.

```json
{
    "elements": ["a", "b", "c"]
}
```

This is equivalent to:

-   a + b + c

##### Paired Multiple Combination

The paired multiple combination will generate all unique possible pairs from the given ingredients. The format is the same as a multiple combination, except a boolean flag called `paired` is set to `true`.

```json
{
    "elements": ["a", "b", "c"],
    "paired": true
}
```

This is equivalent to:

-   a + b
-   a + c
-   b + c

##### Grouped Combination

The grouped combination will generate all unique possible pairs from two groups of ingredients, by choosing one element from the first group, and the second element from the other group.

```json
{
    "first elements": ["a", "b"],
    "second elements": ["c", "d"]
}
```

This is equivalent to:

-   a + c
-   a + d
-   b + c
-   b + d

##### Shorthand Combination

The shorthand combination is a shorthand way of describing multiple combinations into one object. Order matters for this type of combination.

```json
{
    "first set": ["a", "b"],
    "second set": ["c", "d"]
}
```

This is equivalent to:

-   a + c
-   b + d

#### Variations

Variations are used to dynamically change an element's texture and/or it's display name.

##### Animation Variation

```json
{
    "type": "animation",
    "textures": [
        "frame_0",
        "frame_1",
        "frame_2",
        "frame_3",
        "frame_4",
        "frame_5"
    ],
    "names": ["frame 0's name"],
    "time": 2000
}
```

Since animations are already supported in other variations, this variation should only be used when the functionality of other variations are not needed.

`textures` is an array of strings, which are the file names of the frames of the animation. The order they are listed in will be the order displayed in the animation. The files must be in the same folder as the element's default texture (you can learn more about textures here).

`names` is an optional array of strings which are the unlocalized names for the corresponding texture. If no name is specified, or if there's an error, the default name will be used instead.

`time` is used to specify the time spent on each frame in milliseconds. By default it is 1000 (1 second per frame).

##### Combination Variation

```json
{
    "type": "combo",
    "textures": [
        {
            "combos": [
                {
                    "first element": "sky",
                    "second element": "sea"
                }
            ],
            "texture": "horizon_sea",
            "name": "name"
        },
        {
            "combos": [
                {
                    "first element": "sky",
                    "second element": "sky"
                }
            ],
            "textures": ["horizon_sea", "air"],
            "time": 1000,
            "names": ["name", "name for air"]
        }
    ]
}
```

This variation is used to change the element's texture when a specified combination occurs. In this example, the `horizon_sea` texture is used when `sky` and `sea` are combined.

`textures` is an array of objects, with each object specifying the combinations needed and the corresponding texture or animation.

`combos` is an array of combination objects which are covered in more detail [here](#combinations).

`name` is an optional string which is the unlocalized name for that texture.

##### Inherit Variation

```json
{
    "type": "inherit",
    "texture": "alchemy:air"
}
```

This variation is used when you want the element to have a pre-existing texture. In this case, the element will use the texture of `air` in the default Alchemy pack.

Note that this will not work if the said element is removed so beware of packs that do so.

##### Date Variation

There are 2 types of date variations currently available: month and week.

```json
{
    "type": "month",
    "textures": [
        "constellation_0",
        { "texture": "constellation_1" },
        { "textures": ["constellation_2_0", "constellation_2_1"] },
        "constellation_3",
        "constellation_4",
        "constellation_5",
        "constellation_6",
        "constellation_7",
        "constellation_8",
        "constellation_9",
        "constellation_10",
        "constellation_11"
    ],
    "names": ["january_name", "february_name"]
}
```

The month variation allows separate textures or animations for every month. Since there are 12 months in a year, there should be 12 file names in the `textures` array. The first texture will be for January, the second texture for February and so on.

```json
{
    "type": "week",
    "textures": [
        "image1",
        { "texture": "image2" },
        { "textures": ["image3", "image3_1"] },
        "image4",
        "image5",
        "image6",
        "image7"
    ],
    "names": ["monday_name", "tuesday_name", "wednesday_name"]
}
```

The week variation allows separate textures or animations for every day of the week. Since there are 7 days in a week, there should be 7 file names in the `textures` array. The first texture will be for Monday, the second texture for Tuesday and so on.

`names` is an optional array of strings which are the unlocalized names for the corresponding month/week texture. If no name is specified, or if there's an error, the default name will be used instead.

##### Random Variation

```json
{
    "type": "random",
    "textures": [
        {
            "weight": "0.25",
            "texture": "dog_emmy",
            "name": "optional_name"
        },
        {
            "weight": "0.5",
            "textures": ["dog_albert", "dog_foo"],
            "time": 200
        }
    ]
}
```

The variation is useful when you want to change the texture based on chance. The `textures` array hold a list of objects, which specifies the probability of the texture or animation being chosen, and the file name. The sum of all of the `weight`s should be between 0 and 1. If it isn't, the results will not be as expected. The remaining probability (in this example 0.5) will be the probability of the default texture being used.

`name` is an optional string which is the unlocalized name for that texture.

#### Removal Structure

There are 4 types of removals.

##### Remove All

```json
{
    "remove": "all",
    "except": ["alchemy:air"]
}
```

The example above will remove all elements **and** combinations, except the elements specified in `except` and combinations containing elements in `except`.

##### Remove Element

```json
{
    "remove": "element",
    "element": "alchemy:air"
}
```

This is used to remove an element, which is specified in `element`.

##### Remove Combination

```json
{
    "remove": "combo",
    "combo": {
        "first element": "123",
        "second element": "456"
    }
}
```

This is used to remove a specific combination that corresponds to the object `combo`.

##### Remove Random Combination

```json
{
    "remove": "random",
    "combo": {
        "elements": ["thing", "thing 2", "thing 3"]
    }
}
```

This will remove the combination specified in `combo` from all random combinations (since random combinations can have more than one combination).

## Textures

All textures used must be in `png` format and be in the correct location.

### Elements

The element textures should be in the following structure:

```
+-- elements
|   +-- namespace
|   |   +-- group id
|   |   |   +-- element_id.png
|   |   |   +-- image1.png
|   |   |   +-- image2.png
|   |   |   +-- ...
```

Since namespaces are used, you can alter the textures of the elements in other packs, like a texture pack. Make sure that the element textures are under their respective group.

### Groups

The group textures follow a similar structure:

```
+-- groups
|   +-- namespace
|   |   +-- group_id.png
|   |   +-- another_group_id.png
|   |   +-- ...
```
