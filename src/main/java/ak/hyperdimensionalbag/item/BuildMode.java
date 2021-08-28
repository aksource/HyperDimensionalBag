package ak.hyperdimensionalbag.item;

/**
 * Created by A.K. on 2021/08/26.
 */
public enum BuildMode {
  EXCHANGE,
  WALL,
  PILLAR,
  CUBE;
  public static final ak.hyperdimensionalbag.item.BuildMode[] MODES = {EXCHANGE, WALL, PILLAR, CUBE};

  public static ak.hyperdimensionalbag.item.BuildMode getMode(int index) {
    return MODES[index % MODES.length];
  }

  public static int getMODESLength() {
    return MODES.length;
  }
}
