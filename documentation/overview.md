# Light Sleeper

## A Simple Result Set Mapper

Light Sleeper is a light alternative to Hibernate. The name signifies the
fact that the framework is much more lightweight than Hibernate, but
also want to point to the fact that the developer has to be more awake. 
Light Sleeper takes less responsibility than Hibernate and hands it back to the
developer. This does in effect mean more code in some places, but it also gives
more transparency as less happens "behind the scene".

The project seeks to be less complex than your standard Java library - this
achieved by not trying to solve all the worlds problems with one framework.

The problems solved by Light Sleeper is

 1. Mapping ResultSets to Java Objects
 2. Generating SQL Queries
 3. Generating Insert SQL
 
That's it!

There's no EntityManager, no transaction management, no connection handling.
Everything is done with plain JDBC - this means you can use any framework
you would normally use with JDBC.


## Usage

 1. Annotate your classes
 2. Use SqlQueryDsl to select the objects 

### Example

    @Table(name = "customer")
    public class Customer {
      @Column(name = "id", primaryKey = true)
      private Integer id;
      
      @Column(name = "name")
      private String name;
      
      ... constructor get/set as usual
    }
    
    public List<Customer> findAll() {
      Connection connection = yourConnectionHandler.getConnection();
      return SqlQueryDsl.build(Customer.class).selectAll(connection);
    }

For full examples see the source in examples.jar.