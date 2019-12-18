# Element Combination Specification

An `elements.json` file is used to define the elements for your pack.

## Structure

```json
[
    {
        "name": "element_id",
        "group": "foo",
        "combos": [
            {
                //combo objects
            }
        ],
        "tags": [
            "bar"
        ]
    }
]
```

## Referring Elements

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

## Combinations

The combinations (combos) of an element are defined with JSON objects, known as "combo objects". There are several types of combo objects, with each yielding a different set of combinations.

### Normal Combination

The normal combination is when you only need two elements (ingredients) to create the element that you are defining right now. Since you must specify exactly two elements, you cannot use groups or tags here as they will not be parsed.

```json
{
    "first element": "a",
    "second element": "b"
}
```

This is equivalent to:

-   a + b

### Multiple Combination

The multiple combination is when you need more than two ingredients.

```json
{
    "elements": ["a", "b", "c"]
}
```

This is equivalent to:

-   a + b + c

### Paired Multiple Combination

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

### Grouped Combination

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

### Shorthand Combination

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