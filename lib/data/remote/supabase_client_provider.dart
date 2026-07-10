import 'package:supabase_flutter/supabase_flutter.dart';

// Acceso global al cliente ya inicializado por Supabase.initialize() en main.dart
SupabaseClient get supabaseClient => Supabase.instance.client;

const kTableGlucoseReadings = 'glucose_readings';
