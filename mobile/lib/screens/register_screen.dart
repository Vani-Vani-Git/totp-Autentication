import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../services/auth_service.dart';
import 'login_screen.dart';

class RegisterScreen extends StatefulWidget {
  const RegisterScreen({super.key});

  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen>
    with TickerProviderStateMixin {
  late final AnimationController _backgroundController;
  late final AnimationController _contentController;

  final AuthService _authService = AuthService();

  final _userIdController = TextEditingController();
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();
  final _confirmPasswordController = TextEditingController();

  final _userIdFocusNode = FocusNode();
  final _emailFocusNode = FocusNode();
  final _passwordFocusNode = FocusNode();
  final _confirmPasswordFocusNode = FocusNode();

  bool _obscurePassword = true;
  bool _obscureConfirmPassword = true;
  bool _isRegistering = false;

  @override
  void initState() {
    super.initState();

    _backgroundController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 14),
    )..repeat();

    _contentController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 850),
    )..forward();
  }

  @override
  void dispose() {
    _backgroundController.dispose();
    _contentController.dispose();

    _userIdController.dispose();
    _emailController.dispose();
    _passwordController.dispose();
    _confirmPasswordController.dispose();

    _userIdFocusNode.dispose();
    _emailFocusNode.dispose();
    _passwordFocusNode.dispose();
    _confirmPasswordFocusNode.dispose();

    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final size = MediaQuery.sizeOf(context);
    final bottomInset = MediaQuery.viewInsetsOf(context).bottom;

    return Scaffold(
      backgroundColor: const Color(0xFF050816),
      resizeToAvoidBottomInset: true,
      body: Stack(
        children: [
          Positioned.fill(
            child: _buildAnimatedBackground(size),
          ),

          SafeArea(
            child: LayoutBuilder(
              builder: (context, constraints) {
                return SingleChildScrollView(
                  physics: const BouncingScrollPhysics(),
                  padding: EdgeInsets.fromLTRB(
                    20,
                    24,
                    20,
                    28 + bottomInset,
                  ),
                  child: ConstrainedBox(
                    constraints: BoxConstraints(
                      minHeight: constraints.maxHeight - 48,
                    ),
                    child: Center(
                      child: FadeTransition(
                        opacity: CurvedAnimation(
                          parent: _contentController,
                          curve: Curves.easeOut,
                        ),
                        child: SlideTransition(
                          position: Tween<Offset>(
                            begin: const Offset(0, 0.05),
                            end: Offset.zero,
                          ).animate(
                            CurvedAnimation(
                              parent: _contentController,
                              curve: Curves.easeOutCubic,
                            ),
                          ),
                          child: _buildRegisterCard(),
                        ),
                      ),
                    ),
                  ),
                );
              },
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
        final progress = _backgroundController.value;

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
              size: size.width * 0.90,
              left: -size.width * 0.35 +
                  progress * size.width * 0.12,
              top: -size.width * 0.28,
              opacity: 0.16,
            ),

            _buildGlowOrb(
              size: size.width * 0.70,
              right: -size.width * 0.28 -
                  progress * size.width * 0.10,
              bottom: size.height * 0.18,
              opacity: 0.11,
            ),

            CustomPaint(
              size: Size.infinite,
              painter: _RegisterGridPainter(
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

  Widget _buildRegisterCard() {
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
            22,
            28,
            22,
            24,
          ),
          decoration: BoxDecoration(
            color: const Color(0xFF101A2C).withValues(
              alpha: 0.88,
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
                  alpha: 0.35,
                ),
                blurRadius: 45,
                offset: const Offset(0, 20),
              ),
            ],
          ),
          child: Column(
            children: [
              _buildHeader(),

              const SizedBox(height: 26),

              _buildTextField(
                controller: _userIdController,
                focusNode: _userIdFocusNode,
                label: 'User ID',
                hint: 'Choose a unique user ID',
                icon: Icons.person_outline_rounded,
                textInputAction: TextInputAction.next,
                onSubmitted: (_) {
                  _emailFocusNode.requestFocus();
                },
              ),

              const SizedBox(height: 14),

              _buildTextField(
                controller: _emailController,
                focusNode: _emailFocusNode,
                label: 'Email',
                hint: 'Enter your email address',
                icon: Icons.email_outlined,
                keyboardType: TextInputType.emailAddress,
                textInputAction: TextInputAction.next,
                onSubmitted: (_) {
                  _passwordFocusNode.requestFocus();
                },
              ),

              const SizedBox(height: 14),

              _buildTextField(
                controller: _passwordController,
                focusNode: _passwordFocusNode,
                label: 'Password',
                hint: 'Create a strong password',
                icon: Icons.lock_outline_rounded,
                obscureText: _obscurePassword,
                textInputAction: TextInputAction.next,
                suffixIcon: IconButton(
                  onPressed: () {
                    setState(() {
                      _obscurePassword =
                          !_obscurePassword;
                    });
                  },
                  icon: Icon(
                    _obscurePassword
                        ? Icons.visibility_outlined
                        : Icons.visibility_off_outlined,
                    color: Colors.white.withValues(
                      alpha: 0.55,
                    ),
                  ),
                ),
                onSubmitted: (_) {
                  _confirmPasswordFocusNode
                      .requestFocus();
                },
              ),

              const SizedBox(height: 14),

              _buildTextField(
                controller: _confirmPasswordController,
                focusNode: _confirmPasswordFocusNode,
                label: 'Confirm Password',
                hint: 'Re-enter your password',
                icon: Icons.lock_reset_outlined,
                obscureText: _obscureConfirmPassword,
                textInputAction: TextInputAction.done,
                suffixIcon: IconButton(
                  onPressed: () {
                    setState(() {
                      _obscureConfirmPassword =
                          !_obscureConfirmPassword;
                    });
                  },
                  icon: Icon(
                    _obscureConfirmPassword
                        ? Icons.visibility_outlined
                        : Icons.visibility_off_outlined,
                    color: Colors.white.withValues(
                      alpha: 0.55,
                    ),
                  ),
                ),
                onSubmitted: (_) {
                  _register();
                },
              ),

              const SizedBox(height: 24),

              _buildRegisterButton(),

              const SizedBox(height: 20),

              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    'Already have an account?',
                    style: TextStyle(
                      color: Colors.white.withValues(
                        alpha: 0.52,
                      ),
                      fontSize: 13,
                    ),
                  ),
                  TextButton(
                    onPressed: _goToLogin,
                    child: const Text(
                      'Sign In',
                      style: TextStyle(
                        color: Color(0xFF73BBFF),
                        fontSize: 13,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ),
                ],
              ),

              const SizedBox(height: 2),

              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(
                    Icons.shield_outlined,
                    size: 14,
                    color: Colors.white.withValues(
                      alpha: 0.30,
                    ),
                  ),
                  const SizedBox(width: 6),
                  Text(
                    'Protected with TOTP security',
                    style: TextStyle(
                      color: Colors.white.withValues(
                        alpha: 0.34,
                      ),
                      fontSize: 11.5,
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildHeader() {
    return Column(
      children: [
        Container(
          width: 72,
          height: 72,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            color: const Color(0xFF1677FF).withValues(
              alpha: 0.12,
            ),
            border: Border.all(
              color: const Color(0xFF69B4FF).withValues(
                alpha: 0.25,
              ),
            ),
            boxShadow: [
              BoxShadow(
                color: const Color(0xFF1677FF).withValues(
                  alpha: 0.14,
                ),
                blurRadius: 28,
                spreadRadius: 4,
              ),
            ],
          ),
          child: const Icon(
            Icons.person_add_alt_1_rounded,
            color: Color(0xFF73BBFF),
            size: 34,
          ),
        ),

        const SizedBox(height: 18),

        const Text(
          'Create Account',
          textAlign: TextAlign.center,
          style: TextStyle(
            color: Colors.white,
            fontSize: 27,
            fontWeight: FontWeight.w700,
            letterSpacing: -0.5,
          ),
        ),

        const SizedBox(height: 8),

        Text(
          'Create your secure TOTP account',
          textAlign: TextAlign.center,
          style: TextStyle(
            color: Colors.white.withValues(
              alpha: 0.62,
            ),
            fontSize: 14,
          ),
        ),
      ],
    );
  }

  Widget _buildTextField({
    required TextEditingController controller,
    required FocusNode focusNode,
    required String label,
    required String hint,
    required IconData icon,
    bool obscureText = false,
    TextInputType? keyboardType,
    TextInputAction? textInputAction,
    Widget? suffixIcon,
    ValueChanged<String>? onSubmitted,
  }) {
    return TextField(
      controller: controller,
      focusNode: focusNode,
      obscureText: obscureText,
      keyboardType: keyboardType,
      textInputAction: textInputAction,
      onSubmitted: onSubmitted,
      style: const TextStyle(
        color: Colors.white,
        fontSize: 14.5,
      ),
      inputFormatters: label == 'User ID'
          ? [
              FilteringTextInputFormatter.deny(
                RegExp(r'\s'),
              ),
            ]
          : null,
      decoration: InputDecoration(
        labelText: label,
        hintText: hint,
        prefixIcon: Icon(
          icon,
          color: Colors.white.withValues(
            alpha: 0.58,
          ),
          size: 21,
        ),
        suffixIcon: suffixIcon,
        labelStyle: TextStyle(
          color: Colors.white.withValues(
            alpha: 0.58,
          ),
        ),
        hintStyle: TextStyle(
          color: Colors.white.withValues(
            alpha: 0.28,
          ),
          fontSize: 13,
        ),
        filled: true,
        fillColor: Colors.white.withValues(
          alpha: 0.055,
        ),
        contentPadding:
            const EdgeInsets.symmetric(
          horizontal: 15,
          vertical: 17,
        ),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: BorderSide(
            color: Colors.white.withValues(
              alpha: 0.07,
            ),
          ),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: BorderSide(
            color: Colors.white.withValues(
              alpha: 0.07,
            ),
          ),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(
            color: Color(0xFF4DA3FF),
            width: 1.3,
          ),
        ),
      ),
    );
  }

  Widget _buildRegisterButton() {
    return SizedBox(
      width: double.infinity,
      height: 55,
      child: ElevatedButton(
        onPressed:
            _isRegistering ? null : _register,
        style: ElevatedButton.styleFrom(
          elevation: 0,
          backgroundColor:
              const Color(0xFF5868AD),
          disabledBackgroundColor:
              const Color(0xFF5868AD).withValues(
            alpha: 0.55,
          ),
          foregroundColor: Colors.white,
          shape: RoundedRectangleBorder(
            borderRadius:
                BorderRadius.circular(16),
          ),
        ),
        child: _isRegistering
            ? const SizedBox(
                width: 22,
                height: 22,
                child:
                    CircularProgressIndicator(
                  strokeWidth: 2.4,
                  valueColor:
                      AlwaysStoppedAnimation<Color>(
                    Colors.white,
                  ),
                ),
              )
            : const Row(
                mainAxisAlignment:
                    MainAxisAlignment.center,
                children: [
                  Icon(
                    Icons.person_add_alt_1_rounded,
                    size: 19,
                  ),
                  SizedBox(width: 9),
                  Text(
                    'Create Account',
                    style: TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ],
              ),
      ),
    );
  }

  Future<void> _register() async {
    final userId =
        _userIdController.text.trim();

    final email =
        _emailController.text.trim();

    final password =
        _passwordController.text;

    final confirmPassword =
        _confirmPasswordController.text;

    if (userId.isEmpty ||
        email.isEmpty ||
        password.isEmpty ||
        confirmPassword.isEmpty) {
      _showMessage(
        'Please complete all fields.',
      );
      return;
    }

    if (password != confirmPassword) {
      _showMessage(
        'Passwords do not match.',
      );
      return;
    }

    if (password.length < 8) {
      _showMessage(
        'Password must contain at least 8 characters.',
      );
      return;
    }

    setState(() {
      _isRegistering = true;
    });

    try {
      final response =
          await _authService.register(
        userId: userId,
        email: email,
        password: password,
      );

      if (!mounted) {
        return;
      }

      if (response.status != 'ACTIVE') {
        throw Exception(
          'Account registration was not completed.',
        );
      }

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text(
            'Account created successfully. Please sign in.',
          ),
        ),
      );

      await Future<void>.delayed(
        const Duration(milliseconds: 700),
      );

      if (!mounted) {
        return;
      }

      Navigator.of(context).pushAndRemoveUntil(
        MaterialPageRoute(
          builder: (_) => const LoginScreen(),
        ),
        (route) => false,
      );
    } catch (error) {
      if (!mounted) {
        return;
      }

      _showMessage(
        error.toString().replaceFirst(
              'Exception: ',
              '',
            ),
      );
    } finally {
      if (mounted) {
        setState(() {
          _isRegistering = false;
        });
      }
    }
  }

  void _showMessage(String message) {
    if (!mounted) {
      return;
    }

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
      ),
    );
  }

  void _goToLogin() {
    Navigator.of(context).pop();
  }
}

class _RegisterGridPainter
    extends CustomPainter {
  final double progress;

  const _RegisterGridPainter({
    required this.progress,
  });

  @override
  void paint(
    Canvas canvas,
    Size size,
  ) {
    final paint = Paint()
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
        paint,
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
        paint,
      );
    }

    final nodePaint = Paint()
      ..style = PaintingStyle.fill
      ..color = const Color(0xFF5AAFFF)
          .withValues(alpha: 0.05);

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
    covariant _RegisterGridPainter oldDelegate,
  ) {
    return oldDelegate.progress != progress;
  }
}