import 'dart:ui';

import 'package:flutter/material.dart';
import '../core/secure_storage_service.dart';
import 'login_screen.dart';

class WelcomeScreen extends StatefulWidget {
  final String accessToken;

  const WelcomeScreen({
    super.key,
    required this.accessToken,
  });

  @override
  State<WelcomeScreen> createState() =>
      _WelcomeScreenState();
}

class _WelcomeScreenState extends State<WelcomeScreen>
    with TickerProviderStateMixin {
  late final AnimationController _backgroundController;
  late final AnimationController _contentController;
  late final AnimationController _pulseController;

  @override
  void initState() {
    super.initState();

    _backgroundController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 14),
    )..repeat();

    _contentController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 900),
    )..forward();

    _pulseController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1800),
    )..repeat(reverse: true);
  }

  @override
  void dispose() {
    _backgroundController.dispose();
    _contentController.dispose();
    _pulseController.dispose();

    super.dispose();
  }
  Future<void> _logout() async {
  final shouldLogout = await showDialog<bool>(
    context: context,
    builder: (context) {
      return AlertDialog(
        title: const Text('Sign out?'),
        content: const Text(
          'You will need to authenticate again to access your account.',
        ),
        actions: [
          TextButton(
            onPressed: () {
              Navigator.of(context).pop(false);
            },
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () {
              Navigator.of(context).pop(true);
            },
            child: const Text('Logout'),
          ),
        ],
      );
    },
  );

  if (shouldLogout != true || !mounted) {
    return;
  }

  final storage = SecureStorageService();

  await storage.clearTokens();

  if (!mounted) {
    return;
  }

  Navigator.of(context).pushAndRemoveUntil(
    MaterialPageRoute(
      builder: (_) => const LoginScreen(),
    ),
    (route) => false,
  );
}
  @override
  Widget build(BuildContext context) {
    final size = MediaQuery.sizeOf(context);

    return Scaffold(
      backgroundColor: const Color(0xFF060A18),
      body: Stack(
        children: [
          Positioned.fill(
            child: _buildAnimatedBackground(size),
          ),

          SafeArea(
            child: Column(
              children: [
                _buildTopBar(),

                Expanded(
                  child: _buildWelcomeContent(),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAnimatedBackground(Size size) {
    return AnimatedBuilder(
      animation: _backgroundController,
      builder: (context, child) {
        final progress =
            _backgroundController.value;

        return Stack(
          children: [
            Container(
              decoration: const BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: [
                    Color(0xFF071326),
                    Color(0xFF0A1730),
                    Color(0xFF050816),
                  ],
                ),
              ),
            ),

            _buildGlowOrb(
              size: size.width * 0.95,
              left: -size.width * 0.35 +
                  progress * size.width * 0.12,
              top: -size.width * 0.25,
              opacity: 0.16,
            ),

            _buildGlowOrb(
              size: size.width * 0.75,
              right: -size.width * 0.30 -
                  progress * size.width * 0.10,
              bottom: size.height * 0.18,
              opacity: 0.11,
            ),

            _buildGlowOrb(
              size: size.width * 0.55,
              left: size.width * 0.20,
              bottom: -size.width * 0.25,
              opacity: 0.08,
            ),

            CustomPaint(
              size: Size.infinite,
              painter: _WelcomeGridPainter(
                progress: progress,
              ),
            ),
          ],
        );
      },
    );
  }

  Widget _buildGlowOrb({
    required double size,
    double? left,
    double? right,
    double? top,
    double? bottom,
    required double opacity,
  }) {
    return Positioned(
      left: left,
      right: right,
      top: top,
      bottom: bottom,
      child: IgnorePointer(
        child: Container(
          width: size,
          height: size,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            gradient: RadialGradient(
              colors: [
                const Color(0xFF318BFF).withValues(
                  alpha: opacity,
                ),
                Colors.transparent,
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildTopBar() {
    return Padding(
      padding: const EdgeInsets.fromLTRB(
        20,
        12,
        20,
        0,
      ),
      child: Row(
        children: [
          const Icon(
            Icons.security_rounded,
            color: Color(0xFF69B4FF),
            size: 24,
          ),
          const SizedBox(width: 10),
          const Expanded(
            child: Text(
              'TOTP Authentication',
              style: TextStyle(
                color: Colors.white,
                fontSize: 18,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
          IconButton(
  onPressed: _logout,
  tooltip: 'Logout',
  icon: const Icon(
    Icons.logout_rounded,
    color: Color(0xFF9CCBFF),
    size: 21,
  ),
  style: IconButton.styleFrom(
    backgroundColor: Colors.white.withValues(
      alpha: 0.06,
    ),
    side: BorderSide(
      color: Colors.white.withValues(
        alpha: 0.08,
      ),
    ),
    shape: RoundedRectangleBorder(
      borderRadius: BorderRadius.circular(12),
    ),
    padding: const EdgeInsets.all(8),
  ),
),
        ],
      ),
    );
  }

  Widget _buildWelcomeContent() {
    return Center(
      child: SingleChildScrollView(
        physics: const BouncingScrollPhysics(),
        padding: const EdgeInsets.symmetric(
          horizontal: 22,
          vertical: 30,
        ),
        child: FadeTransition(
          opacity: CurvedAnimation(
            parent: _contentController,
            curve: Curves.easeOut,
          ),
          child: SlideTransition(
            position: Tween<Offset>(
              begin: const Offset(0, 0.08),
              end: Offset.zero,
            ).animate(
              CurvedAnimation(
                parent: _contentController,
                curve: Curves.easeOutCubic,
              ),
            ),
            child: _buildWelcomeCard(),
          ),
        ),
      ),
    );
  }

  Widget _buildWelcomeCard() {
    return ClipRRect(
      borderRadius: BorderRadius.circular(30),
      child: BackdropFilter(
        filter: ImageFilter.blur(
          sigmaX: 18,
          sigmaY: 18,
        ),
        child: Container(
          width: double.infinity,
          constraints: const BoxConstraints(
            maxWidth: 440,
          ),
          padding: const EdgeInsets.fromLTRB(
            24,
            34,
            24,
            30,
          ),
          decoration: BoxDecoration(
            color: const Color(0xFF101A2C).withValues(
              alpha: 0.84,
            ),
            borderRadius: BorderRadius.circular(30),
            border: Border.all(
              color: Colors.white.withValues(
                alpha: 0.10,
              ),
            ),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withValues(
                  alpha: 0.30,
                ),
                blurRadius: 42,
                offset: const Offset(0, 20),
              ),
            ],
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              _buildSuccessIcon(),

              const SizedBox(height: 26),

              const Text(
                'Welcome',
                textAlign: TextAlign.center,
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 32,
                  fontWeight: FontWeight.w700,
                  letterSpacing: -0.6,
                ),
              ),

              const SizedBox(height: 10),

              Text(
                'You have successfully signed in.',
                textAlign: TextAlign.center,
                style: TextStyle(
                  color: Colors.white.withValues(
                    alpha: 0.70,
                  ),
                  fontSize: 15,
                  height: 1.5,
                ),
              ),

              const SizedBox(height: 26),

              _buildSecurityStatus(),

              const SizedBox(height: 26),

              Text(
                'Your account is protected with '
                'two-factor authentication.',
                textAlign: TextAlign.center,
                style: TextStyle(
                  color: Colors.white.withValues(
                    alpha: 0.48,
                  ),
                  fontSize: 12.5,
                  height: 1.5,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildSuccessIcon() {
    return AnimatedBuilder(
      animation: _pulseController,
      builder: (context, child) {
        final scale =
            1.0 + (_pulseController.value * 0.045);

        return Transform.scale(
          scale: scale,
          child: Container(
            width: 94,
            height: 94,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: const Color(0xFF39D98A).withValues(
                alpha: 0.10,
              ),
              border: Border.all(
                color: const Color(0xFF59E7A0).withValues(
                  alpha: 0.30,
                ),
                width: 1.2,
              ),
              boxShadow: [
                BoxShadow(
                  color: const Color(0xFF39D98A).withValues(
                    alpha: 0.15,
                  ),
                  blurRadius: 32,
                  spreadRadius: 5,
                ),
              ],
            ),
            child: Container(
              margin: const EdgeInsets.all(12),
              decoration: const BoxDecoration(
                shape: BoxShape.circle,
                color: Color(0xFF39D98A),
              ),
              child: const Icon(
                Icons.check_rounded,
                color: Colors.white,
                size: 42,
              ),
            ),
          ),
        );
      },
    );
  }

  Widget _buildSecurityStatus() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(
        horizontal: 17,
        vertical: 15,
      ),
      decoration: BoxDecoration(
        color: const Color(0xFF39D98A).withValues(
          alpha: 0.07,
        ),
        borderRadius: BorderRadius.circular(17),
        border: Border.all(
          color: const Color(0xFF39D98A).withValues(
            alpha: 0.16,
          ),
        ),
      ),
      child: Row(
        children: [
          Container(
            width: 38,
            height: 38,
            decoration: BoxDecoration(
              color: const Color(0xFF39D98A).withValues(
                alpha: 0.12,
              ),
              borderRadius: BorderRadius.circular(12),
            ),
            child: const Icon(
              Icons.shield_rounded,
              color: Color(0xFF59E7A0),
              size: 21,
            ),
          ),

          const SizedBox(width: 13),

          const Expanded(
            child: Column(
              crossAxisAlignment:
                  CrossAxisAlignment.start,
              children: [
                Text(
                  'Authentication successful',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 13.5,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                SizedBox(height: 3),
                Text(
                  'Your identity has been verified.',
                  style: TextStyle(
                    color: Color(0xFF8DA19B),
                    fontSize: 11.5,
                  ),
                ),
              ],
            ),
          ),

          const Icon(
            Icons.check_circle_rounded,
            color: Color(0xFF59E7A0),
            size: 21,
          ),
        ],
      ),
    );
  }
}

class _WelcomeGridPainter extends CustomPainter {
  final double progress;

  const _WelcomeGridPainter({
    required this.progress,
  });

  @override
  void paint(
    Canvas canvas,
    Size size,
  ) {
    final gridPaint = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = 0.7
      ..color = Colors.white.withValues(
        alpha: 0.022,
      );

    const spacing = 48.0;

    final shift = progress * spacing;

    for (
      double x = -spacing + shift;
      x < size.width + spacing;
      x += spacing
    ) {
      canvas.drawLine(
        Offset(x, 0),
        Offset(x, size.height),
        gridPaint,
      );
    }

    for (
      double y = -spacing + shift;
      y < size.height + spacing;
      y += spacing
    ) {
      canvas.drawLine(
        Offset(0, y),
        Offset(size.width, y),
        gridPaint,
      );
    }

    final nodePaint = Paint()
      ..style = PaintingStyle.fill
      ..color = const Color(0xFF5AAFFF).withValues(
        alpha: 0.055,
      );

    for (
      double x = spacing;
      x < size.width;
      x += spacing * 2
    ) {
      for (
        double y = spacing;
        y < size.height;
        y += spacing * 2
      ) {
        canvas.drawCircle(
          Offset(x, y),
          1.4,
          nodePaint,
        );
      }
    }
  }

  @override
  bool shouldRepaint(
    covariant _WelcomeGridPainter oldDelegate,
  ) {
    return oldDelegate.progress != progress;
  }
}