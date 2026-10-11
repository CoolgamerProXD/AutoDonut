package net.minecraft.network.chat;
import net.minecraft.ChatFormatting;
public class MutableComponent implements Component {
  public String getString() { return ""; }
  public MutableComponent copy() { return this; }
  public MutableComponent append(Component c) { return this; }
  public MutableComponent append(String s) { return this; }
  public MutableComponent withStyle(ChatFormatting f) { return this; }
  public MutableComponent withStyle(ChatFormatting... f) { return this; }
  public MutableComponent withStyle(Style s) { return this; }
}
