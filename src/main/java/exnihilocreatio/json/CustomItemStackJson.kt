package exnihilocreatio.json

import com.google.gson.*
import exnihilocreatio.util.LogUtil
import exnihilocreatio.util.doInline
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.JsonToNBT
import net.minecraft.nbt.NBTException
import java.lang.reflect.Type

object CustomItemStackJson : JsonDeserializer<ItemStack>, JsonSerializer<ItemStack> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ItemStack {
        val helper = JsonHelper(json)

        val name = helper.getString("name")
        val amount = helper.getNullableInteger("amount", 1)
        val meta = helper.getNullableInteger("meta", 0)

        val item: Item = Item.getByNameOrId(name) ?: doInline {
            LogUtil.error("Error parsing JSON: Invalid Item: " + json.toString())
            LogUtil.error("This may result in crashing or other undefined behavior")

            return@doInline Items.AIR
        }

        val stack = ItemStack(item, amount, meta)

        if (json.asJsonObject.has("nbt")) {
            try {
                stack.tagCompound = JsonToNBT.getTagFromJson(json.asJsonObject.get("nbt").asString)
            } catch (e: NBTException) {
                LogUtil.error("Could not convert JSON to NBT: " + json.asJsonObject.get("nbt").asString, e)
                e.printStackTrace()
            }
        }

        return stack
    }

    override fun serialize(src: ItemStack, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject()

        jsonObject.addProperty("name", if (src.item.registryName == null) "" else src.item.registryName!!.toString())
        jsonObject.addProperty("amount", src.count)
        jsonObject.addProperty("meta", src.itemDamage)

        val nbt = src.tagCompound
        if (nbt != null && !nbt.isEmpty) {
            jsonObject.addProperty("nbt", nbt.toString())
        }

        return jsonObject
    }
}
