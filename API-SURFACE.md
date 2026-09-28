# External API surface

Every Minecraft, Fabric and library member AutoDonut depends on, in one place.

**106 external types, 387 members.**

## Why this file exists

AutoDonut is fully type-checked (see `tools/offline-typecheck/`), but that
check compiles against *stubs* of the APIs below. The stubs are our best
understanding of the Minecraft 26.2 / Fabric signatures - they are not the
real thing. So this is the review checklist: if `./gradlew build` fails, the
mismatch is somewhere in this list and the compiler error will name the type.

Everything here is a **read-only dependency**. AutoDonut calls these; it does
not patch, mixin or otherwise modify them - `mixins` is empty in
`fabric.mod.json`. Nothing in this list is a server-side API: the mod declares
`"environment": "client"` and has no `main` entrypoint.

The list is kept minimal automatically. Any stub that can be deleted while the
project still compiles is deleted, so every entry below is genuinely reached
by AutoDonut's code.


## Minecraft

*73 types, 315 members*


### `net.minecraft.ChatFormatting`

*(type reference only - no members used)*


### `net.minecraft.client.KeyMapping`

```java
public KeyMapping(String name, com.mojang.blaze3d.platform.InputConstants.Type type, int code, Category category)
public KeyMapping(String name, int code, String category)
public boolean consumeClick()
public boolean isDown()
public static Category register(net.minecraft.resources.Identifier id)
```

### `net.minecraft.client.Minecraft`

```java
public static Minecraft getInstance()
public net.minecraft.client.player.LocalPlayer player
public net.minecraft.client.multiplayer.ClientLevel level
public net.minecraft.client.gui.Gui gui
public net.minecraft.client.multiplayer.MultiPlayerGameMode gameMode
public net.minecraft.client.gui.Font font
public net.minecraft.client.gui.screens.Screen screen
public net.minecraft.world.phys.HitResult hitResult
public void setScreen(net.minecraft.client.gui.screens.Screen s)
public void execute(Runnable r)
public boolean isSingleplayer()
```

### `net.minecraft.client.gui.Font`

```java
public int width(String s)
public int width(net.minecraft.network.chat.Component c)
public int lineHeight = 9
```

### `net.minecraft.client.gui.Gui`

```java
public net.minecraft.client.gui.screens.Screen screen()
public void setScreen(net.minecraft.client.gui.screens.Screen s)
```

### `net.minecraft.client.gui.GuiGraphics`

```java
public void drawString(Font font, net.minecraft.network.chat.Component text, int x, int y, int colour)
public void drawString(Font font, String text, int x, int y, int colour)
public void drawCenteredString(Font font, net.minecraft.network.chat.Component text, int x, int y, int colour)
public void drawCenteredString(Font font, String text, int x, int y, int colour)
public void fill(int x1, int y1, int x2, int y2, int colour)
```

### `net.minecraft.client.gui.components.AbstractWidget`

```java
public boolean visible = true, active = true
public int getX()
public void setX(int x)
public int getWidth()
public void setWidth(int w)
public void setMessage(net.minecraft.network.chat.Component m)
public net.minecraft.network.chat.Component getMessage()
public void setTooltip(Object t)
public void visible(boolean v)
```

### `net.minecraft.client.gui.components.Button`

```java
public static Builder builder(Component message, OnPress onPress)
public Builder bounds(int x, int y, int w, int h)
public Builder pos(int x, int y)
public Builder width(int w)
public Builder size(int w, int h)
public Builder tooltip(Object tooltip)
public Button build()
```

### `net.minecraft.client.gui.components.Checkbox`

```java
public boolean selected()
public static Builder builder(Component message, net.minecraft.client.gui.Font font)
public Builder pos(int x, int y)
public Builder selected(boolean b)
public Builder onValueChange(OnValueChange cb)
public Builder maxWidth(int w)
public Builder tooltip(Object t)
public Checkbox build()
```

### `net.minecraft.client.gui.components.EditBox`

```java
public EditBox(net.minecraft.client.gui.Font font, int x, int y, int w, int h, Component message)
public void setValue(String v)
public String getValue()
public void setMaxLength(int n)
public void setResponder(java.util.function.Consumer<String> r)
public void setEditable(boolean b)
public void setHint(Component c)
```

### `net.minecraft.client.gui.components.MultiLineTextWidget`

```java
public MultiLineTextWidget(Component message, net.minecraft.client.gui.Font font)
public MultiLineTextWidget(int x, int y, Component message, net.minecraft.client.gui.Font font)
public MultiLineTextWidget setMaxWidth(int w)
public MultiLineTextWidget setColor(int c)
public MultiLineTextWidget setCentered(boolean b)
```

### `net.minecraft.client.gui.components.StringWidget`

```java
public StringWidget(Component message, net.minecraft.client.gui.Font font)
public StringWidget(int x, int y, int w, int h, Component message, net.minecraft.client.gui.Font font)
public StringWidget alignLeft()
public StringWidget alignCenter()
```

### `net.minecraft.client.gui.screens.Screen`

```java
protected Component title
public int width, height
public Minecraft minecraft
public Font font
protected Screen(Component title)
protected void init()
public void onClose()
public void render(net.minecraft.client.gui.GuiGraphics g, int mx, int my, float dt)
public void tick()
public boolean isPauseScreen()
public Component getTitle()
protected <T extends net.minecraft.client.gui.components.AbstractWidget> T addRenderableWidget(T widget)
public void resize(Minecraft mc, int w, int h)
public net.minecraft.client.gui.Font getFont()
```

### `net.minecraft.client.gui.screens.inventory.AbstractContainerScreen`

```java
protected AbstractContainerScreen(T menu, net.minecraft.world.entity.player.Inventory inv, net.minecraft.network.chat.Component title)
public T getMenu()
```

### `net.minecraft.client.multiplayer.ClientLevel`

*(type reference only - no members used)*


### `net.minecraft.client.multiplayer.ClientPacketListener`

```java
public void send(Object packet)
public void sendCommand(String command)
public void sendChat(String message)
```

### `net.minecraft.client.multiplayer.MultiPlayerGameMode`

```java
public boolean startDestroyBlock(BlockPos pos, Direction face)
public boolean continueDestroyBlock(BlockPos pos, Direction face)
public void stopDestroyBlock()
public InteractionResult useItemOn(net.minecraft.client.player.LocalPlayer player, InteractionHand hand, BlockHitResult hit)
public void handleContainerInput(int containerId, int slot, int button, ContainerInput type, Player player)
public boolean isDestroying()
public InteractionResult useItem(net.minecraft.world.entity.player.Player player, InteractionHand hand)
```

### `net.minecraft.client.player.ClientInput`

```java
public net.minecraft.world.entity.player.Input keyPresses = net.minecraft.world.entity.player.Input.EMPTY
```

### `net.minecraft.client.player.LocalPlayer`

```java
public net.minecraft.client.multiplayer.ClientPacketListener connection
public ClientInput input
```

### `net.minecraft.commands.CommandBuildContext`

*(type reference only - no members used)*


### `net.minecraft.core.BlockPos`

```java
public static final BlockPos ZERO = new BlockPos(0,0,0)
public BlockPos(int x, int y, int z)
public int getX()
public BlockPos above()
public BlockPos below()
public BlockPos north()
public BlockPos east()
public BlockPos offset(int x, int y, int z)
public BlockPos relative(Direction d)
public BlockPos relative(Direction d, int n)
public BlockPos immutable()
public double distSqr(BlockPos other)
public double distToCenterSqr(net.minecraft.world.phys.Vec3 v)
public static Iterable<BlockPos> betweenClosed(BlockPos a, BlockPos b)
public static Iterable<BlockPos> betweenClosed(int x1,int y1,int z1,int x2,int y2,int z2)
public int distManhattan(BlockPos o)
public MutableBlockPos()
public MutableBlockPos(int x,int y,int z)
public MutableBlockPos set(int x,int y,int z)
public MutableBlockPos setWithOffset(BlockPos p,int x,int y,int z)
```

### `net.minecraft.core.Direction`

```java
public float toYRot()
public Direction getOpposite()
public int getStepX()
public static Direction getApproximateNearest(double x, double y, double z)
public static Direction fromYRot(double yRot)
public Direction getClockWise()
public Direction getCounterClockWise()
```

### `net.minecraft.core.NonNullList`

```java
public static <E> NonNullList<E> withSize(int size, E fill)
public static <E> NonNullList<E> create()
public E get(int i)
public E set(int i, E e)
public int size()
```

### `net.minecraft.core.Registry`

```java
static <V, T extends V> T register(Registry<V> registry, net.minecraft.resources.Identifier id, T value)
static <V, T extends V> T register(Registry<V> registry, net.minecraft.resources.ResourceKey<V> key, T value)
static <V, T extends V> T register(Registry<V> registry, String id, T value)
```

### `net.minecraft.core.component.DataComponentType`

*(type reference only - no members used)*


### `net.minecraft.core.component.DataComponents`

```java
public static final DataComponentType<Object> FOOD = null
public static final DataComponentType<Object> CONSUMABLE = null
public static final DataComponentType<Integer> DAMAGE = null
public static final DataComponentType<Integer> MAX_DAMAGE = null
```

### `net.minecraft.core.registries.BuiltInRegistries`

```java
public static final net.minecraft.core.Registry<net.minecraft.world.item.Item> ITEM = null
public static final net.minecraft.core.Registry<net.minecraft.world.level.block.Block> BLOCK = null
public static final net.minecraft.core.Registry<net.minecraft.world.level.block.entity.BlockEntityType<?>> BLOCK_ENTITY_TYPE = null
```

### `net.minecraft.network.chat.Component`

```java
static MutableComponent literal(String text)
static MutableComponent translatable(String key)
static MutableComponent empty()
```

### `net.minecraft.network.chat.MutableComponent`

```java
public String getString()
public MutableComponent copy()
public MutableComponent append(Component c)
public MutableComponent append(String s)
public MutableComponent withStyle(ChatFormatting f)
public MutableComponent withStyle(ChatFormatting... f)
public MutableComponent withStyle(Style s)
```

### `net.minecraft.network.chat.Style`

*(type reference only - no members used)*


### `net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket`

*(type reference only - no members used)*


### `net.minecraft.resources.Identifier`

```java
public static Identifier fromNamespaceAndPath(String ns, String path)
public static Identifier parse(String s)
public String getPath()
public String getNamespace()
```

### `net.minecraft.resources.ResourceKey`

```java
public static <T> ResourceKey<T> create(ResourceKey<net.minecraft.core.Registry<T>> registry, Identifier id)
public Identifier location()
```

### `net.minecraft.world.Container`

```java
default int getMaxStackSize()
default void clearContent()
default boolean canPlaceItem(int slot, ItemStack stack)
```

### `net.minecraft.world.InteractionHand`

*(type reference only - no members used)*


### `net.minecraft.world.InteractionResult`

*(type reference only - no members used)*


### `net.minecraft.world.entity.Entity`

```java
public BlockPos blockPosition()
public Vec3 position()
public Vec3 getEyePosition()
public double getX()
public float getYRot()
public void setYRot(float y)
public void sendSystemMessage(Component message)
public Component getName()
public net.minecraft.world.level.Level level()
public double distanceToSqr(Entity other)
public double distanceToSqr(double x, double y, double z)
public boolean isAlive()
public boolean onGround()
public net.minecraft.world.phys.AABB getBoundingBox()
public boolean isUsingItem()
```

### `net.minecraft.world.entity.monster.Monster`

*(type reference only - no members used)*


### `net.minecraft.world.entity.player.FoodData`

*(type reference only - no members used)*


### `net.minecraft.world.entity.player.Input`

```java
public static final Input EMPTY = new Input(false,false,false,false,false,false,false)
```

### `net.minecraft.world.entity.player.Inventory`

```java
public int selected
public ItemStack getItem(int slot)
public void setItem(int slot, ItemStack stack)
public int getContainerSize()
public int getSelectedSlot()
public void setSelectedSlot(int slot)
public ItemStack getSelected()
public boolean add(ItemStack stack)
public int getFreeSlot()
```

### `net.minecraft.world.entity.player.Player`

```java
public Inventory getInventory()
public net.minecraft.world.inventory.AbstractContainerMenu containerMenu
public net.minecraft.world.inventory.AbstractContainerMenu inventoryMenu
public double blockInteractionRange()
public double entityInteractionRange()
public void closeContainer()
public ItemStack getMainHandItem()
public ItemStack getOffhandItem()
public float getHealth()
public float getMaxHealth()
public FoodData getFoodData()
public boolean isShiftKeyDown()
public boolean isCreative()
public boolean addItem(net.minecraft.world.item.ItemStack stack)
public void displayClientMessage(net.minecraft.network.chat.Component msg, boolean actionBar)
```

### `net.minecraft.world.inventory.AbstractContainerMenu`

```java
public int containerId
public NonNullList<Slot> slots = NonNullList.create()
public Slot getSlot(int i)
public ItemStack getCarried()
public java.util.List<ItemStack> getItems()
```

### `net.minecraft.world.inventory.ContainerInput`

*(type reference only - no members used)*


### `net.minecraft.world.inventory.InventoryMenu`

*(type reference only - no members used)*


### `net.minecraft.world.inventory.Slot`

```java
public final int index = 0
public ItemStack getItem()
public boolean hasItem()
public net.minecraft.world.Container container
public int getContainerSlot()
```

### `net.minecraft.world.item.Item`

```java
public net.minecraft.network.chat.Component getName()
public Item asItem()
public Properties stacksTo(int n)
public Properties setId(net.minecraft.resources.ResourceKey<Item> id)
public Properties useBlockDescriptionPrefix()
```

### `net.minecraft.world.item.ItemStack`

```java
public static final ItemStack EMPTY = new ItemStack()
public ItemStack()
public ItemStack(Item item)
public ItemStack(Item item, int count)
public ItemStack(net.minecraft.world.level.ItemLike item)
public boolean isEmpty()
public int getCount()
public void setCount(int n)
public Item getItem()
public Component getHoverName()
public Component getDisplayName()
public ItemStack copy()
public boolean is(Item item)
public int getDamageValue()
public int getMaxDamage()
public boolean isDamageableItem()
public int getMaxStackSize()
public void shrink(int n)
public java.util.List<Component> getTooltipLines(Item.TooltipContext ctx, net.minecraft.world.entity.player.Player player, TooltipFlag flag)
public boolean has(net.minecraft.core.component.DataComponentType<?> type)
public <T> T get(net.minecraft.core.component.DataComponentType<T> type)
public static boolean isSameItemSameComponents(ItemStack a, ItemStack b)
public void grow(int n)
public ItemStack split(int n)
```

### `net.minecraft.world.item.Items`

```java
public static final Item AIR=null, COAL=null, CHARCOAL=null, COAL_BLOCK=null, BLAZE_ROD=null,
```

### `net.minecraft.world.item.TooltipFlag`

*(type reference only - no members used)*


### `net.minecraft.world.level.ItemLike`

*(type reference only - no members used)*


### `net.minecraft.world.level.Level`

```java
public BlockState getBlockState(BlockPos pos)
public BlockEntity getBlockEntity(BlockPos pos)
public net.minecraft.world.level.material.FluidState getFluidState(BlockPos pos)
public boolean isLoaded(BlockPos pos)
public boolean isClientSide()
public boolean isClientSide = true
public boolean setBlock(BlockPos pos, BlockState state, int flags)
public boolean setBlockAndUpdate(BlockPos pos, BlockState state)
public boolean destroyBlock(BlockPos pos, boolean drop)
public int getMinY()
public int getMaxY()
public <T extends net.minecraft.world.entity.Entity> java.util.List<T> getEntitiesOfClass(Class<T> c, net.minecraft.world.phys.AABB box)
public long getGameTime()
public java.util.List<net.minecraft.world.entity.Entity> getEntities(net.minecraft.world.entity.Entity except, net.minecraft.world.phys.AABB box, java.util.function.Predicate<? super net.minecraft.world.entity.Entity> filter)
public boolean removeBlock(net.minecraft.core.BlockPos pos, boolean isMoving)
```

### `net.minecraft.world.level.block.Block`

```java
public Block(Properties props)
public net.minecraft.world.level.block.state.BlockState defaultBlockState()
public net.minecraft.world.item.Item asItem()
public String getDescriptionId()
public static void popResource(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.minecraft.world.item.ItemStack stack)
protected static <B extends Block> com.mojang.serialization.MapCodec<B> simpleCodec(java.util.function.Function<Properties, B> f)
```

### `net.minecraft.world.level.block.Blocks`

```java
public static final Block AIR=null, STONE=null, FURNACE=null, BLAST_FURNACE=null, SMOKER=null,
public static final Block NETHERITE_BLOCK = null, IRON_BLOCK = null, GOLD_BLOCK = null, DIAMOND_BLOCK = null
```

### `net.minecraft.world.level.block.CropBlock`

```java
public CropBlock(Properties p)
public boolean isMaxAge(BlockState state)
public BlockState getStateForAge(int age)
public int getMaxAge()
public net.minecraft.world.level.block.state.properties.IntegerProperty getAgeProperty()
```

### `net.minecraft.world.level.block.NetherWartBlock`

```java
public NetherWartBlock(Properties p)
public static final net.minecraft.world.level.block.state.properties.IntegerProperty AGE = null
```

### `net.minecraft.world.level.block.SoundType`

*(type reference only - no members used)*


### `net.minecraft.world.level.block.entity.BlockEntity`

```java
protected BlockPos worldPosition
protected net.minecraft.world.level.Level level
public BlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
public BlockPos getBlockPos()
public net.minecraft.world.level.Level getLevel()
public BlockState getBlockState()
public void setChanged()
protected void loadAdditional(net.minecraft.world.level.storage.ValueInput in)
protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput out)
```

### `net.minecraft.world.level.block.entity.BlockEntityType`

*(type reference only - no members used)*


### `net.minecraft.world.level.block.state.BlockBehaviour`

```java
protected BlockBehaviour(Properties props)
protected net.minecraft.world.InteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state,
public static Properties of()
public Properties strength(float s)
public Properties strength(float a, float b)
public Properties requiresCorrectToolForDrops()
public Properties mapColor(net.minecraft.world.level.material.MapColor c)
public Properties sound(net.minecraft.world.level.block.SoundType s)
public Properties lightLevel(java.util.function.ToIntFunction<BlockState> f)
public Properties setId(net.minecraft.resources.ResourceKey<net.minecraft.world.level.block.Block> id)
public Properties noOcclusion()
```

### `net.minecraft.world.level.block.state.BlockState`

```java
public boolean isAir()
public net.minecraft.world.level.block.Block getBlock()
public float getDestroySpeed(Object level, BlockPos pos)
public net.minecraft.world.phys.shapes.VoxelShape getCollisionShape(Object level, BlockPos pos)
public boolean is(net.minecraft.world.level.block.Block block)
public <T extends Comparable<T>> T getValue(net.minecraft.world.level.block.state.properties.Property<T> p)
public <T extends Comparable<T>, V extends T> BlockState setValue(net.minecraft.world.level.block.state.properties.Property<T> p, V v)
public boolean hasProperty(net.minecraft.world.level.block.state.properties.Property<?> p)
public net.minecraft.world.level.material.FluidState getFluidState()
```

### `net.minecraft.world.level.block.state.properties.BlockStateProperties`

```java
public static final IntegerProperty AGE_1 = null, AGE_2 = null, AGE_3 = null, AGE_5 = null,
```

### `net.minecraft.world.level.block.state.properties.IntegerProperty`

*(type reference only - no members used)*


### `net.minecraft.world.level.block.state.properties.Property`

*(type reference only - no members used)*


### `net.minecraft.world.level.material.FluidState`

*(type reference only - no members used)*


### `net.minecraft.world.level.material.MapColor`

*(type reference only - no members used)*


### `net.minecraft.world.level.storage.ValueInput`

*(type reference only - no members used)*


### `net.minecraft.world.level.storage.ValueOutput`

*(type reference only - no members used)*


### `net.minecraft.world.phys.AABB`

```java
public AABB(double x1,double y1,double z1,double x2,double y2,double z2)
public AABB(net.minecraft.core.BlockPos pos)
public AABB inflate(double d)
public static AABB ofSize(Vec3 centre, double x, double y, double z)
```

### `net.minecraft.world.phys.BlockHitResult`

```java
public BlockHitResult(Vec3 location, net.minecraft.core.Direction direction, net.minecraft.core.BlockPos pos, boolean inside)
public net.minecraft.core.BlockPos getBlockPos()
public net.minecraft.core.Direction getDirection()
```

### `net.minecraft.world.phys.HitResult`

```java
public Type getType()
public Vec3 getLocation()
```

### `net.minecraft.world.phys.Vec3`

```java
public final double x, y, z
public Vec3(double x, double y, double z)
public static Vec3 atCenterOf(net.minecraft.core.BlockPos pos)
public static Vec3 atLowerCornerOf(net.minecraft.core.BlockPos pos)
public Vec3 add(double x, double y, double z)
public Vec3 subtract(Vec3 o)
public Vec3 normalize()
public double distanceTo(Vec3 o)
public double distanceToSqr(Vec3 o)
public double length()
```

### `net.minecraft.world.phys.shapes.VoxelShape`

*(type reference only - no members used)*


## Fabric API / Loader

*11 types, 12 members*


### `net.fabricmc.api.ClientModInitializer`

*(type reference only - no members used)*


### `net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback`

*(type reference only - no members used)*


### `net.fabricmc.fabric.api.client.command.v2.ClientCommands`

```java
public static LiteralArgumentBuilder<FabricClientCommandSource> literal(String name)
public static <T> RequiredArgumentBuilder<FabricClientCommandSource, T> argument(String name, ArgumentType<T> type)
```

### `net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource`

*(type reference only - no members used)*


### `net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents`

```java
public static final Event<EndTick> END_CLIENT_TICK = null
public static final Event<StartTick> START_CLIENT_TICK = null
```

### `net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper`

```java
public static KeyMapping registerKeyMapping(KeyMapping mapping)
```

### `net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents`

```java
public static final Event<AllowGame> ALLOW_GAME = null
public static final Event<AllowChat> ALLOW_CHAT = null
public static final Event<Chat> CHAT = null
```

### `net.fabricmc.fabric.api.client.screen.v1.ScreenEvents`

```java
public static final Event<AfterInit> AFTER_INIT = null
public static final Event<BeforeInit> BEFORE_INIT = null
```

### `net.fabricmc.fabric.api.client.screen.v1.Screens`

```java
public static java.util.List<AbstractWidget> getWidgets(Screen screen)
```

### `net.fabricmc.fabric.api.event.Event`

*(type reference only - no members used)*


### `net.fabricmc.loader.api.FabricLoader`

```java
static FabricLoader getInstance()
```

## Brigadier (commands)

*11 types, 17 members*


### `com.mojang.brigadier.Command`

*(type reference only - no members used)*


### `com.mojang.brigadier.CommandDispatcher`

```java
public Object register(com.mojang.brigadier.builder.LiteralArgumentBuilder<S> b)
```

### `com.mojang.brigadier.arguments.ArgumentType`

*(type reference only - no members used)*


### `com.mojang.brigadier.arguments.BoolArgumentType`

```java
public static BoolArgumentType bool()
public static boolean getBool(com.mojang.brigadier.context.CommandContext<?> c, String n)
```

### `com.mojang.brigadier.arguments.DoubleArgumentType`

```java
public static DoubleArgumentType doubleArg()
public static DoubleArgumentType doubleArg(double min)
public static DoubleArgumentType doubleArg(double min, double max)
public static double getDouble(com.mojang.brigadier.context.CommandContext<?> c, String n)
```

### `com.mojang.brigadier.arguments.IntegerArgumentType`

```java
public static IntegerArgumentType integer()
public static IntegerArgumentType integer(int min)
public static IntegerArgumentType integer(int min, int max)
public static int getInteger(com.mojang.brigadier.context.CommandContext<?> c, String n)
```

### `com.mojang.brigadier.arguments.StringArgumentType`

```java
public static StringArgumentType word()
public static StringArgumentType string()
public static StringArgumentType greedyString()
public static String getString(com.mojang.brigadier.context.CommandContext<?> c, String n)
```

### `com.mojang.brigadier.builder.ArgumentBuilder`

*(type reference only - no members used)*


### `com.mojang.brigadier.builder.LiteralArgumentBuilder`

*(type reference only - no members used)*


### `com.mojang.brigadier.builder.RequiredArgumentBuilder`

*(type reference only - no members used)*


### `com.mojang.brigadier.context.CommandContext`

```java
public S getSource()
public <T> T getArgument(String name, Class<T> clazz)
```

## Mojang libraries

*2 types, 3 members*


### `com.mojang.blaze3d.platform.InputConstants`

```java
public static final int KEY_K = 75
public static final Key UNKNOWN = new Key()
public static class Key
```

### `com.mojang.serialization.MapCodec`

*(type reference only - no members used)*


## Mod Menu (optional)

*2 types, 1 members*


### `com.terraformersmc.modmenu.api.ConfigScreenFactory`

*(type reference only - no members used)*


### `com.terraformersmc.modmenu.api.ModMenuApi`

```java
default ConfigScreenFactory<?> getModConfigScreenFactory()
```

## Third-party libraries

*7 types, 39 members*


### `com.google.gson.Gson`

```java
public String toJson(Object src)
public void toJson(Object src, Appendable w)
public <T> T fromJson(String json, Class<T> c)
public <T> T fromJson(java.io.Reader r, Class<T> c)
public <T> T fromJson(String json, java.lang.reflect.Type t)
public <T> T fromJson(java.io.Reader r, java.lang.reflect.Type t)
```

### `com.google.gson.GsonBuilder`

```java
public GsonBuilder setPrettyPrinting()
public GsonBuilder disableHtmlEscaping()
public GsonBuilder serializeNulls()
public Gson create()
```

### `com.google.gson.JsonArray`

```java
public void add(JsonElement e)
public void add(String s)
public void add(Number n)
public JsonElement get(int i)
public int size()
public java.util.Iterator<JsonElement> iterator()
public boolean isEmpty()
```

### `com.google.gson.JsonElement`

```java
public JsonObject getAsJsonObject()
public JsonArray getAsJsonArray()
public String getAsString()
public int getAsInt()
public long getAsLong()
public double getAsDouble()
public boolean getAsBoolean()
public boolean isJsonNull()
public boolean isJsonObject()
public boolean isJsonArray()
```

### `com.google.gson.JsonObject`

```java
public void add(String k, JsonElement v)
public void addProperty(String k, String v)
public void addProperty(String k, Number v)
public void addProperty(String k, Boolean v)
public JsonElement get(String k)
public JsonObject getAsJsonObject(String k)
public JsonArray getAsJsonArray(String k)
public boolean has(String k)
public java.util.Set<java.util.Map.Entry<String, JsonElement>> entrySet()
```

### `com.google.gson.JsonParser`

*(type reference only - no members used)*


### `com.google.gson.reflect.TypeToken`

```java
protected TypeToken()
public java.lang.reflect.Type getType()
public static <T> TypeToken<T> get(Class<T> c)
```
