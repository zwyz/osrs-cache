package osrs.unpack.config;

import osrs.unpack.Type;
import osrs.unpack.Unpacker;
import osrs.util.Packet;

import java.util.ArrayList;
import java.util.List;

public class WaterUnpacker {
    public static List<String> unpack(int id, byte[] data) {
        var lines = new ArrayList<String>();
        var packet = new Packet(data);
        lines.add("[" + Unpacker.format(Type.WATER, id) + "]");

        while (true) switch (packet.g1()) {
            case 0 -> {
                if (packet.pos != packet.arr.length) {
                    throw new IllegalStateException("end of file not reached");
                }

                return lines;
            }

            // DecodeFoamParams
            case 9 -> lines.add("water_foam_scale=" + packet.g2());
            case 14 -> lines.add("water_depth_foam=" + packet.g2());

            // DecodeLightingReflectionParams
            case 5 -> lines.add("reflection_strength=" + packet.g2());
            case 25 -> lines.add("specular_shininess=" + packet.g2());
            case 27 -> lines.add("specular_factor=" + packet.g2());
            case 83 -> lines.add("fresnel_bias=" + packet.gFloat());

            // DecodeMaterialScaleParams
            case 2 -> lines.add("normal_map_material1_scale=" + packet.g2());
            case 4 -> lines.add("normal_map_material2_scale=" + packet.g2());
            case 10 -> lines.add("foam_material_scale=" + packet.g2());
            case 32 -> lines.add("normal_map_material3_scale=" + packet.g2());
            case 87 -> lines.add("emissive_map_material_scale=" + packet.g2());

            // DecodeNormalParams
            case 29 -> lines.add("normal_map_material1=" + Unpacker.format(Type.MATERIAL, packet.g2()));
            case 30 -> lines.add("normal_map_material2=" + Unpacker.format(Type.MATERIAL, packet.g2()));
            case 31 -> lines.add("normal_map_material3=" + Unpacker.format(Type.MATERIAL, packet.g2()));

            case 33 -> lines.add("normal_map_a0=" + Unpacker.formatYesNo(packet.g1()));
            case 34 -> lines.add("normal_map_b0=" + packet.gFloat());
            case 35 -> lines.add("normal_map_c0=" + packet.gFloat());
            case 36 -> lines.add("normal_map_d0=" + packet.gFloat());
            case 37 -> lines.add("normal_map_e0=" + packet.gFloat() + "," + packet.gFloat());
            case 38 -> lines.add("normal_map_f0=" + packet.gFloat() + "," + packet.gFloat());
            case 39 -> lines.add("normal_map_f0=" + packet.gFloat());
            case 40 -> lines.add("normal_map_g0=" + packet.gFloat());

            case 41 -> lines.add("normal_map_a1=" + Unpacker.formatYesNo(packet.g1()));
            case 42 -> lines.add("normal_map_b1=" + packet.gFloat());
            case 43 -> lines.add("normal_map_c1=" + packet.gFloat());
            case 44 -> lines.add("normal_map_d1=" + packet.gFloat());
            case 45 -> lines.add("normal_map_e1=" + packet.gFloat() + "," + packet.gFloat());
            case 46 -> lines.add("normal_map_f1=" + packet.gFloat() + "," + packet.gFloat());
            case 47 -> lines.add("normal_map_f1=" + packet.gFloat());
            case 48 -> lines.add("normal_map_g1=" + packet.gFloat());

            case 49 -> lines.add("normal_map_a2=" + Unpacker.formatYesNo(packet.g1()));
            case 50 -> lines.add("normal_map_b2=" + packet.gFloat());
            case 51 -> lines.add("normal_map_c2=" + packet.gFloat());
            case 52 -> lines.add("normal_map_d2=" + packet.gFloat());
            case 53 -> lines.add("normal_map_e2=" + packet.gFloat() + "," + packet.gFloat());
            case 54 -> lines.add("normal_map_f2=" + packet.gFloat() + "," + packet.gFloat());
            case 55 -> lines.add("normal_map_f2=" + packet.gFloat());
            case 56 -> lines.add("normal_map_g2=" + packet.gFloat());

            case 57 -> lines.add("normal_map_a3=" + Unpacker.formatYesNo(packet.g1()));
            case 58 -> lines.add("normal_map_b3=" + packet.gFloat());
            case 59 -> lines.add("normal_map_c3=" + packet.gFloat());
            case 60 -> lines.add("normal_map_d3=" + packet.gFloat());
            case 61 -> lines.add("normal_map_e3=" + packet.gFloat() + "," + packet.gFloat());
            case 62 -> lines.add("normal_map_f3=" + packet.gFloat() + "," + packet.gFloat());
            case 63 -> lines.add("normal_map_f3=" + packet.gFloat());
            case 64 -> lines.add("normal_map_g3=" + packet.gFloat());

            case 65 -> lines.add("normal_map_a4=" + Unpacker.formatYesNo(packet.g1()));
            case 66 -> lines.add("normal_map_b4=" + packet.gFloat());
            case 67 -> lines.add("normal_map_c4=" + packet.gFloat());
            case 68 -> lines.add("normal_map_d4=" + packet.gFloat());
            case 69 -> lines.add("normal_map_e4=" + packet.gFloat() + "," + packet.gFloat());
            case 70 -> lines.add("normal_map_f4=" + packet.gFloat() + "," + packet.gFloat());
            case 71 -> lines.add("normal_map_f4=" + packet.gFloat());
            case 72 -> lines.add("normal_map_g4=" + packet.gFloat());

            case 73 -> lines.add("normal_map_a5=" + Unpacker.formatYesNo(packet.g1()));
            case 74 -> lines.add("normal_map_b5=" + packet.gFloat());
            case 75 -> lines.add("normal_map_c5=" + packet.gFloat());
            case 76 -> lines.add("normal_map_d5=" + packet.gFloat());
            case 77 -> lines.add("normal_map_e5=" + packet.gFloat() + "," + packet.gFloat());
            case 78 -> lines.add("normal_map_f5=" + packet.gFloat() + "," + packet.gFloat());
            case 79 -> lines.add("normal_map_f5=" + packet.gFloat());
            case 80 -> lines.add("normal_map_g5=" + packet.gFloat());

            // DecodeEmissiveParams
            case 86 -> lines.add("emisive_map_material=" + packet.g2());
            case 88 -> lines.add("emissive_uv_scale=" + packet.gFloat() + "," + packet.gFloat());
            case 89 -> lines.add("emissive_rgb=" + packet.g4s());
            case 90 -> lines.add("emissive_scale=" + packet.gFloat());
            case 91 -> lines.add("emissive_map_refraction_depth=" + packet.gFloat());
            case 92 -> lines.add("emissive_map_mode=" + packet.g1());
            case 93 -> lines.add("emissive_source=" + packet.gFloat());
            case 94 -> lines.add("emissive_flow_speed=" + packet.gFloat());
            case 95 -> lines.add("emissive_flow_rotation_degrees=" + packet.gFloat());
            case 96 -> lines.add("emissive_uv_mode=" + packet.g1());
            case 108 -> lines.add("emissive_blend=" + packet.gFloat());

            // DecodeExtinctionParams
            case 97 -> lines.add("extinction_rgb_depth_metres=" + packet.gFloat() + "," + packet.gFloat() + "," + packet.gFloat());
            case 98 -> lines.add("extinction_opaque_water_colour =" + packet.g4s());
            case 99 -> lines.add("extinction_visibility_metres=" + packet.gFloat());

            // DecodeCausticsParams
            case 100 -> lines.add("caustics_scale=" + packet.gFloat());
            case 101 -> lines.add("caustics_refraction_scale=" + packet.gFloat());
            case 102 -> lines.add("caustics_depth_fade_cutoff=" + packet.gFloat());
            case 103 -> lines.add("caustics_depth_fade_scale=" + packet.gFloat());
            case 104 -> lines.add("caustics_edge_fade_start=" + packet.gFloat());
            case 105 -> lines.add("caustics_edge_fade_end=" + packet.gFloat());
            case 106 -> lines.add("caustics_over_water_fade_start=" + packet.gFloat());
            case 107 -> lines.add("caustics_over_water_fade_end=" + packet.gFloat());

            // other
            case 81 -> lines.add("still_water_normal_strength=" + packet.gFloat());
            case 82 -> lines.add("flow_noise=" + packet.gFloat());
            case 85 -> lines.add("override_default_water_type=" + Unpacker.formatYesNo(packet.g1()));

            default -> throw new IllegalStateException("unknown opcode");
        }
    }
}
