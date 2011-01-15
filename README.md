# JOhm

JOhm is a blazingly fast Object-Hash Mapping library for Java inspired by the awesome [Ohm](http://github.com/soveran/ohm). The JOhm OHM is a 
modern-day avatar of the old ORM's like Hibernate with the difference being that we are not dealing with an RDBMS here but with a NoSQL rockstar.

JOhm is a library for storing objects in [Redis](http://github.com/antirez/redis), a persistent key-value database. JOhm is designed to be 
minimally-invasive and relies wholly on reflection aided by annotation hooks for persistence. The fundamental idea is to allow large existing
codebases to easily plug into Redis without the need to extend framework base classes or provide excessive configuration metadata.

Durable data storage is available via the Redis Append-only file (AOF). The default persistence strategy is Snapshotting.

## What can I do with JOhm?
JOhm is still in active development. The following features are currently available:

- Basic attribute persistence (String, Integer, etc...)
- Auto-numeric Ids
- References
- Arrays
- Indexes
- Deletion
- List, Set, SortedSet and Map relationship
- Search on attributes, arrays, collections and references

Stay close! It is growing pretty fast!

## How do I use it?

You can download the latest build at [http://github.com/xetorthio/johm/downloads](http://github.com/xetorthio/johm/downloads)

And this is a small example (getters and setters are not included for the sake of simplicity):
    
    @Model
    class User {
        @Id
        private Long id;
    	@Attribute
    	private String name;
    	@Attribute
    	@Indexed
    	private int age;
    	@Reference
    	@Indexed
    	private Country country;
    	@CollectionList(of = Comment.class)
    	@Indexed
    	private List<Comment> comments;
    	@CollectionSet(of = Item.class)
    	@Indexed
    	private Set<Item> purchases;
    	@CollectionMap(key = Integer.class, value = Item.class)
        @Indexed
        private Map<Integer, Item> favoritePurchases;
        @CollectionSortedSet(of = Item.class, by = "price")
        @Indexed
        private Set<Item> orderedPurchases;
        @Array(of = Item.class, length = 3)
        @Indexed
        private Item[] threeLatestPurchases;
    }

    @Model
	class Comment {
	    @Id
	    private Long id;
    	@Attribute
    	private String text;
	}

    @Model
	class Item {
	    @Id
	    private Long id;
    	@Attribute
    	private String name;
	}

Initiating JOhm:
    jedisPool = new JedisPool(new Config(), "localhost");
    JOhm.setPool(jedisPool);

Creating a User and persisting it:

	User someOne = new User();
	someOne.setName("Someone");
	someOne.setAge(30);
	JOhm.save(someOne);

Loading a persisted User:
	
	User storedUser = JOhm.get(User.class, 1);
	
Deleting a User:

	JOhm.delete(User.class, 1);

Search for all users of age 30:

	List<User> users = JOhm.find(User.class, "age", "30");
	
Model with a reference:

	User someOne = new User();
	...
	JOhm.save(someOne);

	Country someCountry = new Country();
	...
	JOhm.save(country);

	someOne.setCountry(someCountry);

Model with a list of nested models:

	User someOne = new User();
	...
	JOhm.save(someOne);
	
	Comment aComment = new Comment();
	...
	JOhm.save(aComment);
	
	someOne.getComments.add(aComment);

Model with a set of nested models:

	User someOne = new User();
	...
	JOhm.save(someOne);
	
	Item anItem = new Item();
	...
	JOhm.save(anItem);
	
	someOne.getPurchases.add(anItem);

For more usage examples check the tests.

And you are done!

## License

Copyright (c) 2010 Gaurav Sharma and Jonathan Leibiusky

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

