package org.frobino.contra.kernel;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  private static String INTERVALS_TABLE_NAME = "intervals";
  private int fNextQuark = 1;
  private BiMap<Integer, String> fQuarkAndAttribute = HashBiMap.create();
  private Map<Integer, Pair<Long, ITmfStateValue>> fQuarkToOngoingState = new HashMap<>();

  public PostgreSSBuilder() {
    // Create the needed tables:
    // 1) Quark, Attribute
    // 2) Timerange, ...Quark
    this.createTable(QUARK_ATTR_TABLE_NAME, "quark int, attribute varchar(255)");
    this.createTable(
        INTERVALS_TABLE_NAME,
        "duration int8range"); // Columns (i.e. quarks) will be added at runtime
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
    // TODO Auto-generated method stub
    return 0;
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
  public @NonNull List<@NonNull Integer> getQuarks(String... arg0) {
    // TODO Auto-generated method stub
    return null;
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
  public @NonNull List<@NonNull Integer> getSubAttributes(int arg0, boolean arg1) {
    // TODO Auto-generated method stub
    return null;
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
    // TODO Auto-generated method stub
    return null;
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
  public void closeHistory(long arg0) {
    // TODO Auto-generated method stub

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
            INTERVALS_TABLE_NAME, Integer.toString(attributeQuark), columnType)) {
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
        String sql =
            generateInsertSpecificValueSql(
                INTERVALS_TABLE_NAME,
                Integer.toString(attributeQuark),
                ongoingStateValue.unboxValue(),
                ongoingStateStartTime,
                t - 1);
        executeUpdate(sql);
      }

      // put the current value "on-top" (cached)
      fQuarkToOngoingState.put(attributeQuark, new Pair<Long, ITmfStateValue>(t, stateValue));
    }
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
