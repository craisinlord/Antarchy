import fs from "node:fs";
import path from "node:path";
import zlib from "node:zlib";

const TAG = {
    END: 0,
    BYTE: 1,
    SHORT: 2,
    INT: 3,
    LONG: 4,
    FLOAT: 5,
    DOUBLE: 6,
    BYTE_ARRAY: 7,
    STRING: 8,
    LIST: 9,
    COMPOUND: 10,
    INT_ARRAY: 11,
    LONG_ARRAY: 12
};

class Reader {
    constructor(buffer) {
        this.buffer = buffer;
        this.offset = 0;
    }

    byte() { return this.buffer.readInt8(this.offset++); }
    ushort() { const value = this.buffer.readUInt16BE(this.offset); this.offset += 2; return value; }
    short() { const value = this.buffer.readInt16BE(this.offset); this.offset += 2; return value; }
    int() { const value = this.buffer.readInt32BE(this.offset); this.offset += 4; return value; }
    long() { const value = this.buffer.readBigInt64BE(this.offset); this.offset += 8; return value; }
    float() { const value = this.buffer.readFloatBE(this.offset); this.offset += 4; return value; }
    double() { const value = this.buffer.readDoubleBE(this.offset); this.offset += 8; return value; }
    string() { const length = this.ushort(); const value = this.buffer.toString("utf8", this.offset, this.offset + length); this.offset += length; return value; }

    payload(type) {
        switch (type) {
            case TAG.BYTE: return this.byte();
            case TAG.SHORT: return this.short();
            case TAG.INT: return this.int();
            case TAG.LONG: return this.long();
            case TAG.FLOAT: return this.float();
            case TAG.DOUBLE: return this.double();
            case TAG.BYTE_ARRAY: {
                const length = this.int();
                const value = Buffer.from(this.buffer.subarray(this.offset, this.offset + length));
                this.offset += length;
                return value;
            }
            case TAG.STRING: return this.string();
            case TAG.LIST: {
                const elementType = this.byte();
                const length = this.int();
                const items = new Array(length);
                for (let i = 0; i < length; i++) items[i] = this.payload(elementType);
                return { elementType, items };
            }
            case TAG.COMPOUND: {
                const value = {};
                while (true) {
                    const childType = this.byte();
                    if (childType === TAG.END) break;
                    value[this.string()] = { type: childType, value: this.payload(childType) };
                }
                return value;
            }
            case TAG.INT_ARRAY: {
                const length = this.int();
                const value = new Int32Array(length);
                for (let i = 0; i < length; i++) value[i] = this.int();
                return value;
            }
            case TAG.LONG_ARRAY: {
                const length = this.int();
                const value = new Array(length);
                for (let i = 0; i < length; i++) value[i] = this.long();
                return value;
            }
            default: throw new Error(`Unsupported NBT tag type ${type}`);
        }
    }

    root() {
        const type = this.byte();
        if (type !== TAG.COMPOUND) throw new Error(`Expected compound root, got ${type}`);
        this.string();
        return { type, value: this.payload(type) };
    }
}

class Writer {
    constructor() { this.parts = []; }
    push(buffer) { this.parts.push(buffer); }
    byte(value) { const b = Buffer.allocUnsafe(1); b.writeInt8(value); this.push(b); }
    short(value) { const b = Buffer.allocUnsafe(2); b.writeInt16BE(value); this.push(b); }
    ushort(value) { const b = Buffer.allocUnsafe(2); b.writeUInt16BE(value); this.push(b); }
    int(value) { const b = Buffer.allocUnsafe(4); b.writeInt32BE(value); this.push(b); }
    long(value) { const b = Buffer.allocUnsafe(8); b.writeBigInt64BE(BigInt(value)); this.push(b); }
    float(value) { const b = Buffer.allocUnsafe(4); b.writeFloatBE(value); this.push(b); }
    double(value) { const b = Buffer.allocUnsafe(8); b.writeDoubleBE(value); this.push(b); }
    string(value) { const b = Buffer.from(value, "utf8"); this.ushort(b.length); this.push(b); }

    payload(type, value) {
        switch (type) {
            case TAG.BYTE: this.byte(value); break;
            case TAG.SHORT: this.short(value); break;
            case TAG.INT: this.int(value); break;
            case TAG.LONG: this.long(value); break;
            case TAG.FLOAT: this.float(value); break;
            case TAG.DOUBLE: this.double(value); break;
            case TAG.BYTE_ARRAY: this.int(value.length); this.push(value); break;
            case TAG.STRING: this.string(value); break;
            case TAG.LIST:
                this.byte(value.elementType);
                this.int(value.items.length);
                for (const item of value.items) this.payload(value.elementType, item);
                break;
            case TAG.COMPOUND:
                for (const [name, child] of Object.entries(value)) {
                    this.byte(child.type);
                    this.string(name);
                    this.payload(child.type, child.value);
                }
                this.byte(TAG.END);
                break;
            case TAG.INT_ARRAY:
                this.int(value.length);
                for (const item of value) this.int(item);
                break;
            case TAG.LONG_ARRAY:
                this.int(value.length);
                for (const item of value) this.long(item);
                break;
            default: throw new Error(`Unsupported NBT tag type ${type}`);
        }
    }

    root(root) {
        this.byte(TAG.COMPOUND);
        this.string("");
        this.payload(TAG.COMPOUND, root.value);
        return Buffer.concat(this.parts);
    }
}

const inputDir = path.resolve("common-1.21.1/src/main/resources/data/antarchy/structure/kings_tree");
const outputDir = path.resolve("common-1.21.1/src/main/resources/data/antarchy/structure/kings_tree_tiles");
const Y_SHIFT = 22;
const TREE_HEIGHT = 358;
const CENTER_X = 137;
const CENTER_Z = 163;
const sources = [
    { file: "kings_tree_bottom.nbt", offset: [0, -22, 0] },
    { file: "kings_tree1.nbt", offset: [0, 0, 0] },
    { file: "kings_tree2.nbt", offset: [-40, 12, -52] },
    { file: "kings_tree3.nbt", offset: [-40, 149, -52] },
    { file: "kings_tree4.nbt", offset: [-54, 233, -64] }
];
const expectedConnections = [
    ["kings_tree1.nbt", [287, 0, 363], "kings_tree_bottom.nbt", [287, 21, 363]],
    ["kings_tree1.nbt", [274, 11, 363], "kings_tree2.nbt", [314, 0, 415]],
    ["kings_tree2.nbt", [314, 136, 442], "kings_tree3.nbt", [314, 0, 442]],
    ["kings_tree3.nbt", [314, 83, 420], "kings_tree4.nbt", [328, 0, 432]]
];

const intList = (...values) => ({ type: TAG.LIST, value: { elementType: TAG.INT, items: values } });
const doubleList = (...values) => ({ type: TAG.LIST, value: { elementType: TAG.DOUBLE, items: values } });
const floorDiv = (value, divisor) => Math.floor(value / divisor);
const floorMod = (value, divisor) => ((value % divisor) + divisor) % divisor;
const paletteKey = value => JSON.stringify(value, (_, item) => typeof item === "bigint" ? `${item}n` : item);
const tileNamePart = value => value < 0 ? `m${-value}` : `p${value}`;

const tiles = new Map();
const globalPalette = [];
const globalPaletteIds = new Map();
let dataVersion;
let sourceBlockCount = 0;
let skippedMarkerCount = 0;
const jigsaws = new Map();

function getTile(tileX, tileZ) {
    const key = `${tileX},${tileZ}`;
    let tile = tiles.get(key);
    if (!tile) {
        tile = { tileX, tileZ, blocks: new Map(), entities: [] };
        tiles.set(key, tile);
    }
    return tile;
}

function paletteId(stateTag) {
    const key = paletteKey(stateTag);
    let id = globalPaletteIds.get(key);
    if (id === undefined) {
        id = globalPalette.length;
        globalPaletteIds.set(key, id);
        globalPalette.push(stateTag);
    }
    return id;
}

for (const source of sources) {
    const compressed = fs.readFileSync(path.join(inputDir, source.file));
    const root = new Reader(zlib.gunzipSync(compressed)).root();
    dataVersion ??= root.value.DataVersion;
    const paletteTag = root.value.palette;
    if (!paletteTag || paletteTag.type !== TAG.LIST) throw new Error(`${source.file} has no primary palette`);
    const palette = paletteTag.value.items;
    const remap = palette.map(paletteId);
    const blocks = root.value.blocks?.value.items ?? [];
    sourceBlockCount += blocks.length;

    for (const block of blocks) {
        const stateIndex = block.state.value;
        const blockName = palette[stateIndex].Name.value;
        if (blockName === "minecraft:jigsaw") {
            jigsaws.set(`${source.file}:${block.pos.value.items.join(",")}`, {
                state: palette[stateIndex],
                nbt: block.nbt?.value ?? {}
            });
        }
        if (blockName === "minecraft:jigsaw" || blockName === "minecraft:structure_void" || blockName === "minecraft:structure_block") {
            skippedMarkerCount++;
            continue;
        }

        const [x, y, z] = block.pos.value.items;
        const relativeX = x + source.offset[0] - CENTER_X;
        const relativeY = y + source.offset[1] + Y_SHIFT;
        const relativeZ = z + source.offset[2] - CENTER_Z;
        if (relativeY < 0 || relativeY >= TREE_HEIGHT) throw new Error(`${source.file} produced out-of-range Y ${relativeY}`);
        const tileX = floorDiv(relativeX, 16);
        const tileZ = floorDiv(relativeZ, 16);
        const localX = floorMod(relativeX, 16);
        const localZ = floorMod(relativeZ, 16);
        const translated = {
            ...block,
            pos: intList(localX, relativeY, localZ),
            state: { type: TAG.INT, value: remap[stateIndex] }
        };
        getTile(tileX, tileZ).blocks.set(`${localX},${relativeY},${localZ}`, translated);
    }

    const entities = root.value.entities?.value.items ?? [];
    for (const entity of entities) {
        const [blockX, blockY, blockZ] = entity.blockPos.value.items;
        const [x, y, z] = entity.pos.value.items;
        const relativeBlockX = blockX + source.offset[0] - CENTER_X;
        const relativeBlockY = blockY + source.offset[1] + Y_SHIFT;
        const relativeBlockZ = blockZ + source.offset[2] - CENTER_Z;
        const tileX = floorDiv(relativeBlockX, 16);
        const tileZ = floorDiv(relativeBlockZ, 16);
        const tileOriginX = tileX * 16;
        const tileOriginZ = tileZ * 16;
        getTile(tileX, tileZ).entities.push({
            ...entity,
            blockPos: intList(relativeBlockX - tileOriginX, relativeBlockY, relativeBlockZ - tileOriginZ),
            pos: doubleList(
                x + source.offset[0] - CENTER_X - tileOriginX,
                y + source.offset[1] + Y_SHIFT,
                z + source.offset[2] - CENTER_Z - tileOriginZ
            )
        });
    }
}

for (const [parentFile, parentPos, childFile, childPos] of expectedConnections) {
    const parent = sources.find(source => source.file === parentFile);
    const child = sources.find(source => source.file === childFile);
    const parentJigsaw = jigsaws.get(`${parentFile}:${parentPos.join(",")}`);
    const childJigsaw = jigsaws.get(`${childFile}:${childPos.join(",")}`);
    if (!parentJigsaw || !childJigsaw) {
        throw new Error(`Missing expected jigsaw connection between ${parentFile} and ${childFile}`);
    }
    const parentTarget = parentJigsaw.nbt.target?.value;
    const childName = childJigsaw.nbt.name?.value;
    if (parentTarget !== childName) {
        throw new Error(`Jigsaw target mismatch: ${parentFile} targets ${parentTarget}, but ${childFile} is named ${childName}`);
    }
    const parentOrientation = parentJigsaw.state.Properties?.value.orientation?.value;
    const childOrientation = childJigsaw.state.Properties?.value.orientation?.value;
    console.log(`${parentFile} ${parentOrientation} -> ${childFile} ${childOrientation}`);
    const [parentFront, parentTop] = parentOrientation.split("_");
    const [childFront, childTop] = childOrientation.split("_");
    const opposite = { up: "down", down: "up", north: "south", south: "north", east: "west", west: "east" };
    const facingStep = {
        up: [0, 1, 0], down: [0, -1, 0], north: [0, 0, -1],
        south: [0, 0, 1], east: [1, 0, 0], west: [-1, 0, 0]
    };
    const parentWorld = parentPos.map((value, axis) => value + parent.offset[axis]);
    const expectedChildWorld = parentWorld.map((value, axis) => value + facingStep[parentFront][axis]);
    const childWorld = childPos.map((value, axis) => value + child.offset[axis]);
    if (expectedChildWorld.some((value, axis) => value !== childWorld[axis])) {
        throw new Error(`Misaligned jigsaw connection: ${parentFile} expects ${expectedChildWorld}, but ${childFile} is at ${childWorld}`);
    }
    if (opposite[parentFront] !== childFront) {
        throw new Error(`Jigsaw facing mismatch: ${parentOrientation} cannot connect to ${childOrientation}`);
    }
    const joint = parentJigsaw.nbt.joint?.value ?? "rollable";
    if (joint === "aligned" && parentTop !== childTop) {
        throw new Error(`Jigsaw ${parentFile} requires a rotated child: ${parentOrientation} vs ${childOrientation}`);
    }
}

fs.rmSync(outputDir, { recursive: true, force: true });
fs.mkdirSync(outputDir, { recursive: true });
let outputBlockCount = 0;
for (const tile of tiles.values()) {
    if (tile.blocks.size === 0 && tile.entities.length === 0) continue;
    const root = {
        type: TAG.COMPOUND,
        value: {
            DataVersion: dataVersion,
            size: intList(16, TREE_HEIGHT, 16),
            palette: { type: TAG.LIST, value: { elementType: TAG.COMPOUND, items: globalPalette } },
            blocks: { type: TAG.LIST, value: { elementType: TAG.COMPOUND, items: [...tile.blocks.values()] } },
            entities: { type: TAG.LIST, value: { elementType: TAG.COMPOUND, items: tile.entities } }
        }
    };
    outputBlockCount += tile.blocks.size;
    const encoded = new Writer().root(root);
    const name = `${tileNamePart(tile.tileX)}_${tileNamePart(tile.tileZ)}.nbt`;
    fs.writeFileSync(path.join(outputDir, name), zlib.gzipSync(encoded, { level: 9 }));
}

console.log(`Read ${sourceBlockCount.toLocaleString()} blocks from ${sources.length} templates.`);
console.log(`Skipped ${skippedMarkerCount.toLocaleString()} structure marker blocks.`);
console.log(`Wrote ${outputBlockCount.toLocaleString()} blocks in ${tiles.size} chunk-local templates.`);
console.log(`Global palette contains ${globalPalette.length} block states.`);
console.log(`Validated ${expectedConnections.length} matching jigsaw connections.`);
