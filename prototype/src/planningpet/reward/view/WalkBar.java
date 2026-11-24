            while (fm.stringWidth(word) > max) {
                // 띄어쓰기 없이 긴 글은 글자 단위로 자른다.
                int cut = word.length() - 1;
                while (cut > 1 && fm.stringWidth(word.substring(0, cut)) > max) {
                    cut--;
                }
                if (line.length() > 0) {
                    lines.add(line.toString());
                    line = new StringBuilder();
                }
                lines.add(word.substring(0, cut));
                word = word.substring(cut);
            }
            String next = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(next) > max && line.length() > 0) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(next);
            }
        }
        lines.add(line.toString());
        return lines;
    }
}
