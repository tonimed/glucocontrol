enum ReadingTag {
  ayunas('Ayunas'),
  preComida('Pre-comida'),
  postComida('Post-comida'),
  antesDormir('Antes de dormir'),
  otro('Otro');

  const ReadingTag(this.label);
  final String label;

  static ReadingTag fromString(String value) {
    return ReadingTag.values.firstWhere(
      (t) => t.name.toUpperCase() == value.toUpperCase(),
      orElse: () => ReadingTag.otro,
    );
  }
}
