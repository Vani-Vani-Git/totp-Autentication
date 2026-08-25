import 'package:flutter_test/flutter_test.dart';

import 'package:mobile/main.dart';

void main() {
  testWidgets(
    'Login screen is displayed',
    (WidgetTester tester) async {
      await tester.pumpWidget(
        const TotpAuthenticationApp(),
      );

      expect(
        find.text('Welcome Back'),
        findsOneWidget,
      );

      expect(
        find.text('User ID or Email'),
        findsOneWidget,
      );

      expect(
        find.text('Password'),
        findsOneWidget,
      );

      expect(
        find.text('Sign In'),
        findsOneWidget,
      );
    },
  );
}