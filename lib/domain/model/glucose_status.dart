import 'package:glucocontrol/domain/model/glucose_range.dart';
import 'package:glucocontrol/domain/model/glucose_reading.dart';

enum GlucoseStatus { low, normal, high }

extension GlucoseStatusX on GlucoseReading {
  GlucoseStatus glucoseStatus(GlucoseRange range) {
    if (valueMgDl < range.minTarget) return GlucoseStatus.low;
    if (valueMgDl > range.maxTarget) return GlucoseStatus.high;
    return GlucoseStatus.normal;
  }
}
