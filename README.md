# JOhm

JOhm is a Object-hash mapping library for Java inspired on the awesome [Ohm](http://github.com/soveran/ohm).

JOhm is a library for storing objects in [Redis](http://github.com/antirez/redis), a persistent key-value database.

## What can I do with JOhm?
Right now it is still in development. So just the following features are available.

- Basic attribute persistence (String, Integer, etc...)
- Auto-numeric Ids
- Simple References
- Indexes
- Simple finders (on just one attribute)
- Deletion
- List relationship
- Set relationship

Stay close! It is growing pretty fast!

## How do I use it?

You can download the latests build at: 
    [http://github.com/xetorthio/johm/downloads](http://github.com/xetorthio/johm/downloads)

And this is a small example (getters and setters are not included for the sake of simplicity):
    
    class User extends Model {
    	@Attribute
    	private String name;
    	@Attribute
    	@Indexed
    	private int age;
    	@Reference
    	private Country country;
    	@CollectionList(of = Comment.class)
    	private List<Comment> comments;
    	@CollectionSet(of = Item.class)
    	private Set<Item> purchases;
    }

	class Comment extends Model {
    	@Attribute
    	private String text;
	}

	class Item extends Model {
    	@Attribute
    	private String name;
	}

Initiating JOhm:
    jedisPool = new JedisPool("localhost");
    jedisPool.init();
    JOhm.setPool(jedisPool);

Creating a User and persisting it:

	User someOne = new User();
	someOne.setName("Someone");
	someOne.setAge(30);
	someOne.save();

Loading a persisted User:
	
	User storedUser = JOhm.get(User.class, 1);
	
Deleting a User:

	JOhm.delete(User.class, 1);

Search for all users of age 30:

	List<User> users = JOhm.find(User.class, "age", "30");
	
Model with a reference:

	User someOne = new User();
	...
	someOne.save();

	Country someCountry = new Country();
	...
	country.save();

	someOne.setCountry(someCountry);

Model with a list of nested models:

	User someOne = new User();
	...
	someOne.save();
	
	Comment aComment = new Comment();
	...
	aComment.save();
	
	someOne.getComments.add(aComment);

Model with a set of nested models:

	User someOne = new User();
	...
	someOne.save();
	
	Item anItem = new Item();
	...
	anItem.save();
	
	someOne.getPurchases.add(anItem);

For more usage examples check the tests. Soon I will add a nice wiki with everything you should know.

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

