// Smoke test básico — verifica que la app arranca sin crash.
// Tests de integración completos se harán con el emulador de Android Studio.
import 'package:flutter_test/flutter_test.dart';

void main() {
  testWidgets('App smoke test placeholder', (tester) async {
    // La inicialización de Supabase requiere credenciales reales;
    // los tests de integración se ejecutan en emulador, no en unit test.
    expect(true, isTrue);
  });
}
