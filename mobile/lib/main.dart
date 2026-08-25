import 'package:flutter/material.dart';
import 'screens/login_screen.dart';

void main() {
  runApp(const TotpAuthenticationApp());
}

class TotpAuthenticationApp extends StatelessWidget {
  const TotpAuthenticationApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'TOTP Authentication',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        colorSchemeSeed: Colors.indigo,
        scaffoldBackgroundColor:
            const Color(0xFFF6F7FB),
      ),
      home: const LoginScreen(),
    );
  }
}