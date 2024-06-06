package org.frobino.contra.kernel;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.util.Pair;

public class PostgreSSBuilder extends PostgreSQLDatabase implements ITmfStateSystemBuilder {

  private static String QUARK_ATTR_TABLE_NAME = "quark_and_attribute";
  private static String INTERVALS_V1_TABLE_NAME = "intervalsV1";
  private static String INTERVALS_V2_TABLE_NAME = "intervalsV2";
  private int fNextQuark = 1;
  private long fLastUpdateTime = 0L;
  private BiMap<Integer, String> fQuarkAndAttribute = HashBiMap.create();
  private Map<Integer, Pair<Long, ITmfStateValue>> fQuarkToOngoingState = new HashMap<>();

  public PostgreSSBuilder() {
    String envTableName = System.getenv("CONTRA_TABLE_NAME");
    if (envTableName != null) {
      INTERVALS_V2_TABLE_NAME = envTableName;
    }
    // Create the needed tables:
    // 1) Quark, Attribute
    this.createTable(QUARK_ATTR_TABLE_NAME, "quark int, attribute varchar(255)");
    // 2) Timerange, ...Quark
    this.createTable(
        INTERVALS_V1_TABLE_NAME,
        "duration int8range"); // Columns (i.e. quarks) will be added at runtime
    this.createTable(
        INTERVALS_V2_TABLE_NAME,
        "duration int8range, quark int, attribute varchar(255), value varchar(255), type"
            + " varchar(255)");
  }

  @Override
  public void dispose() {
    // TODO Auto-generated method stub

  }

  @Override
  public @NonNull String getAttributeName(int quark) {
    String attribute = fQuarkAndAttribute.get(quark);
    if (attribute != null) {
      int sepPos = attribute.lastIndexOf("/");
      return attribute.substring(0, sepPos);
    }
    return "";
  }

  @Override
  public long getCurrentEndTime() {
    return fLastUpdateTime;
  }

  @Override
  public @NonNull String getFullAttributePath(int arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String @NonNull [] getFullAttributePathArray(int arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getNbAttributes() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public long getOngoingStartTime(int arg0) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getParentAttributeQuark(int arg0) {
    // TODO Auto-generated method stub
    // FIXME?
    return 0;
  }

  @Override
  public int getQuarkAbsolute(String... arg0) throws AttributeNotFoundException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getQuarkRelative(int arg0, String... arg1) throws AttributeNotFoundException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public @NonNull List<@NonNull Integer> getQuarks(String... pattern) {
    // TODO Auto-generated method stub

    Set<String> stringList = fQuarkAndAttribute.values();

    // Define a regex pattern
    String regex = "";
    for (String p : pattern) {
      if (p == "*") {
        p = "A-Za-z0-9";
      }
      if (regex.isEmpty()) {
        regex = regex.concat("[" + p + "]+");
      } else {
        regex = regex.concat("\\/[" + p + "]+");
      }
    }

    // Compile the regex pattern
    Pattern realPattern = Pattern.compile(regex);

    // Iterate through the list and filter strings matching the regex
    List<Integer> matchedQuarks = new ArrayList<>();
    for (String str : stringList) {
      Matcher matcher = realPattern.matcher(str);
      if (matcher.matches()) { // Check if the entire string matches the regex
        int matchedQuark = fQuarkAndAttribute.inverse().get(str);
        matchedQuarks.add(matchedQuark);
      }
    }

    return matchedQuarks;
  }

  @Override
  public @NonNull List<@NonNull Integer> getQuarks(int arg0, String... arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public @NonNull String getSSID() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public long getStartTime() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public @NonNull List<@NonNull Integer> getSubAttributes(int quark, boolean recursive) {
    String attribute = fQuarkAndAttribute.get(quark);
    List<String> subAttributes =
        fQuarkAndAttribute.values().stream()
            .filter(s -> s.startsWith(attribute))
            .collect(Collectors.toList());

    if (!recursive) {
      Set<String> nonRecursivesubAttributes = new HashSet<>();
      for (String subattribute : subAttributes) {
        int attributeLastIndex = subattribute.indexOf(attribute) + attribute.length();
        String sa = subattribute.substring(attributeLastIndex);
        if (sa != "") {
          // System.out.println(sa);
          String[] sarray = sa.split("/");
          nonRecursivesubAttributes.add(attribute + "/" + sarray[1]);
          // System.out.println(sa);
        }
      }
      subAttributes = nonRecursivesubAttributes.stream().collect(Collectors.toList());
    }

    List<Integer> quarksForSubattributes = new ArrayList<>();
    for (String subAttribute : subAttributes) {
      quarksForSubattributes.add(fQuarkAndAttribute.inverse().get(subAttribute));
    }

    return quarksForSubattributes;
  }

  @Override
  public @NonNull List<@NonNull Integer> getSubAttributes(int arg0, boolean arg1, String arg2) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isCancelled() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public int optQuarkAbsolute(String... arg0) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int optQuarkRelative(int arg0, String... arg1) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Iterable<@NonNull ITmfStateInterval> query2D(
      @NonNull Collection<Integer> arg0, @NonNull Collection<Long> arg1)
      throws StateSystemDisposedException, IndexOutOfBoundsException, TimeRangeException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Iterable<@NonNull ITmfStateInterval> query2D(
      @NonNull Collection<Integer> arg0, long arg1, long arg2)
      throws StateSystemDisposedException, IndexOutOfBoundsException, TimeRangeException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public @NonNull List<@NonNull ITmfStateInterval> queryFullState(long arg0)
      throws StateSystemDisposedException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public @NonNull ITmfStateValue queryOngoingState(int arg0) {
    Pair<Long, ITmfStateValue> state = fQuarkToOngoingState.get(arg0);
    if (state == null) {
      return TmfStateValue.newValue(null);
    }
    return state.getSecond();
  }

  @Override
  public @NonNull ITmfStateInterval querySingleState(long arg0, int arg1)
      throws StateSystemDisposedException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void waitUntilBuilt() {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean waitUntilBuilt(long arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void closeHistory(long t) {
    // TODO: If anything to be pushed to the DB, do it now

    // TODO: Push the "quark to attribute" table
    List<String> a = Arrays.asList(new String[] {"quark", "attribute"});

    for (Map.Entry<Integer, String> entry : fQuarkAndAttribute.entrySet()) {
      List<Object> l = new ArrayList<>();
      l.add(entry.getKey());
      l.add(entry.getValue());
      String sql = generateInsertSql(QUARK_ATTR_TABLE_NAME, a, l);
      executeUpdate(sql);
    }
  }

  @Override
  public int getQuarkAbsoluteAndAdd(String... attribute) {
    String fullAttribute = String.join("/", attribute);
    Integer quark = fQuarkAndAttribute.inverse().get(fullAttribute);
    if (quark == null) {
      quark = fNextQuark++;
      fQuarkAndAttribute.put(quark, fullAttribute);
    }
    return quark;
  }

  @Override
  public int getQuarkRelativeAndAdd(int baseQuark, String... attribute) {
    String baseAttribute = fQuarkAndAttribute.get(baseQuark);
    String relativeAttribute = String.join("/", attribute);
    String fullAttribute = baseAttribute + "/" + relativeAttribute;
    Integer quark = fQuarkAndAttribute.inverse().get(fullAttribute);
    if (quark == null) {
      quark = fNextQuark++;
      fQuarkAndAttribute.put(quark, fullAttribute);
    }
    return quark;
  }

  @Override
  public void modifyAttribute(long t, Object value, int attributeQuark)
      throws StateValueTypeException {
    fLastUpdateTime = t;
    // modifyAttributeV1(t, value, attributeQuark);
    modifyAttributeV2(t, value, attributeQuark);
  }

  // First attemt to fill the db with columns: "duration", "quark1", "quark2", ...
  private void modifyAttributeV1(long t, Object value, int attributeQuark) {
    ITmfStateValue stateValue = TmfStateValue.newValue(value);
    String columnType = null;
    // FIXME: the case below is just to make it work now
    switch (stateValue.getType()) {
      case INTEGER:
        columnType = "int";
        break;
      case LONG:
        columnType = "int";
        break;
      case DOUBLE:
        columnType = "int";
        break;
      case STRING:
        columnType = "text";
        break;
      case CUSTOM:
        break;
      case NULL:
        break;
      default:
        break;
    }

    if ((columnType != null)
        && addColumnIfNotExists(
            INTERVALS_V1_TABLE_NAME, Integer.toString(attributeQuark), columnType)) {
      // It is the 1st time we insert this attribute/quark (column) in the DB
      fQuarkToOngoingState.put(attributeQuark, new Pair<Long, ITmfStateValue>(t, stateValue));
    } else {
      /*
       * The attribute/quark (column) is already there. We need to:
       * - check if that attribute/quark has already someting "on-top"
       *   - if YES: then close/insert that interval and then put the current value "on-top" (cached)
       *   - if NO: put the current value "on-top" (cached)
       */
      Pair<Long, ITmfStateValue> ongoingState =
          fQuarkToOngoingState.getOrDefault(
              attributeQuark, new Pair<Long, ITmfStateValue>(0L, TmfStateValue.newValue(null)));
      ITmfStateValue ongoingStateValue = ongoingState.getSecond();
      if (!ongoingStateValue.isNull()) {
        // close/insert the interval
        long ongoingStateStartTime = ongoingState.getFirst();
        Pair<Long, Long> range = new Pair<Long, Long>(ongoingStateStartTime, t - 1);
        List<String> columns = Arrays.asList("duration", Integer.toString(attributeQuark));
        List<Object> values = Arrays.asList(range, ongoingStateValue.unboxValue());
        String sql = generateInsertSql(INTERVALS_V1_TABLE_NAME, columns, values);
        executeUpdate(sql);
      }

      // put the current value "on-top" (cached)
      fQuarkToOngoingState.put(attributeQuark, new Pair<Long, ITmfStateValue>(t, stateValue));
    }
  }

  private void modifyAttributeV2(long t, Object value, int attributeQuark) {
    ITmfStateValue stateValue = TmfStateValue.newValue(value);
    Pair<Long, ITmfStateValue> ongoingState =
        fQuarkToOngoingState.getOrDefault(
            attributeQuark, new Pair<Long, ITmfStateValue>(0L, TmfStateValue.newValue(null)));
    ITmfStateValue ongoingStateValue = ongoingState.getSecond();
    if (!ongoingStateValue.isNull()) {
      // close/insert the interval
      long ongoingStateStartTime = ongoingState.getFirst();
      Pair<Long, Long> range = new Pair<Long, Long>(ongoingStateStartTime, t - 1);
      List<String> columns = Arrays.asList("duration", "quark", "attribute", "value", "type");
      // FIXME: the case below is just to make it work now
      String columnType = null;
      switch (stateValue.getType()) {
        case INTEGER:
          columnType = "int";
          break;
        case LONG:
          columnType = "int";
          break;
        case DOUBLE:
          columnType = "int";
          break;
        case STRING:
          columnType = "text";
          break;
        case CUSTOM:
          break;
        case NULL:
          break;
        default:
          break;
      }
      List<Object> values =
          Arrays.asList(
              range,
              attributeQuark,
              fQuarkAndAttribute.get(attributeQuark),
              ongoingStateValue.unboxValue(),
              columnType);
      String sql = generateInsertSql(INTERVALS_V2_TABLE_NAME, columns, values);
      executeUpdate(sql);
    }

    // put the current value "on-top" (cached)
    fQuarkToOngoingState.put(attributeQuark, new Pair<Long, ITmfStateValue>(t, stateValue));
  }

  @Override
  public ITmfStateValue popAttribute(long arg0, int arg1) throws StateValueTypeException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void pushAttribute(long arg0, Object arg1, int arg2) throws StateValueTypeException {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeAttribute(long arg0, int arg1) {
    // TODO Auto-generated method stub

    //

  }

  @Override
  public void removeFiles() {
    // TODO Auto-generated method stub

  }

  @Override
  public void updateOngoingState(@NonNull ITmfStateValue arg0, int arg1) {
    // TODO Auto-generated method stub

  }
}
