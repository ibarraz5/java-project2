package taskone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskone.StringList;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class StringListTest {

  StringList list;
  private final PrintStream standardOut = System.out;
  private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

  @BeforeEach
  void setUp() {
    System.setOut(new PrintStream(outputStreamCaptor));
    list = new StringList();
    list.add("Ivan");
    list.add("Jack");
    list.add("Sunnie");
    list.add("Simpson");
    list.add("Added");
  }

  @Test
  void add() {

    String list0 = list.getValue(0);
    String list1 = list.getValue(1);
    String list2 = list.getValue(2);
    String list3 = list.getValue(3);
    String list4 = list.getValue(4);

    assertTrue(list0.toString().equalsIgnoreCase("Ivan"));
    assertTrue(list1.toString().equalsIgnoreCase("Jack"));
    assertTrue(list2.toString().equalsIgnoreCase("Sunnie"));
    assertTrue(list3.toString().equalsIgnoreCase("Simpson"));
    assertTrue(list4.toString().equalsIgnoreCase("Added"));

    assertEquals(5, list.size());
    assertNotEquals(10, list.size());

  }

  @Test
  void remove() {

    String list0 = list.getValue(0);
    String list1 = list.getValue(1);
    String list2 = list.getValue(2);
    String list3 = list.getValue(3);
    String list4 = list.getValue(4);

    assertTrue(list0.toString().equalsIgnoreCase("Ivan"));
    assertTrue(list1.toString().equalsIgnoreCase("Jack"));
    assertTrue(list2.toString().equalsIgnoreCase("Sunnie"));
    assertTrue(list3.toString().equalsIgnoreCase("Simpson"));
    assertTrue(list4.toString().equalsIgnoreCase("Added"));

    assertEquals(5, list.size());
    assertTrue(list.remove(0).equalsIgnoreCase("Ivan"));
    assertTrue(list.getValue(0).equalsIgnoreCase("Jack"));
    assertEquals(4, list.size());

    assertEquals(4, list.size());
    assertTrue(list.remove(3).equalsIgnoreCase("Added"));
    assertTrue(list.getValue(2).equalsIgnoreCase("Simpson"));
    assertEquals(3, list.size());


  }

  @Test
  void isWithinBounds() {

    assertTrue(list.isWithinBounds(0));
    assertTrue(list.isWithinBounds(1));
    assertTrue(list.isWithinBounds(2));
    assertTrue(list.isWithinBounds(3));
    assertTrue(list.isWithinBounds(4));

    assertFalse(list.isWithinBounds(-1));
    assertFalse(list.isWithinBounds(5));

  }

  @Test
  void displayList() {
    assertTrue(list.displayList().equalsIgnoreCase("Ivan\nJack\nSunnie\nSimpson\nAdded"));
  }

  @Test
  void reverse() {
    assertTrue(list.reverse(0).equalsIgnoreCase("tnarg\nJack\nSunnie\nSimpson\nAdded"));

  }

  @Test
  void listSizeThenDisplayed() {
    assertTrue(list.size() == 5);
  }

}
