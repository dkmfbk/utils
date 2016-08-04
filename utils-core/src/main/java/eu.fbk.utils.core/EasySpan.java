package eu.fbk.utils.core;

import com.google.common.collect.Lists;
import com.google.common.html.HtmlEscapers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Created by alessio on 24/03/15.
 */
public final class EasySpan implements Comparable<EasySpan> {

	public int begin;

	public int end;

	private static final Logger LOGGER = LoggerFactory.getLogger(EasySpan.class);

	public EasySpan(final String span) {
		final String trimmedSpan = span.trim();
		final int delimiter = trimmedSpan.indexOf(',');
		this.begin = Integer.parseInt(trimmedSpan.substring(0, delimiter));
		this.end = Integer.parseInt(trimmedSpan.substring(delimiter + 1));
	}

	public EasySpan(final int begin, final int end) {
		this.begin = begin;
		this.end = end;
	}

	public String apply(final String text) {
		return apply(text, true);
	}

	public String apply(final String text, boolean escapeHTML) {
		if (escapeHTML) {
			return HtmlEscapers.htmlEscaper().escape(text.substring(this.begin, this.end));
		}
		else {
			return text.substring(this.begin, this.end);
		}
	}

//	public EasySpan align(final String text) {
//
//		int begin = this.begin;
//		int end = this.end;
//
//		while (begin < end && !CorpusPreprocessor.isWord(text.charAt(begin))) {
//			++begin;
//		}
//		while (begin > 0 && !CorpusPreprocessor.isDelim(text.charAt(begin - 1))) {
//			--begin;
//		}
//
//		while (end > begin && !CorpusPreprocessor.isWord(text.charAt(end - 1))) {
//			--end;
//		}
//		while (end < text.length() && !CorpusPreprocessor.isDelim(text.charAt(end))) {
//			++end;
//		}
//
//		return begin == this.begin && end == this.end ? this : new EasySpan(begin, end);
//	}
//
//	public void check(final String text, final String documentURI) {
//		if (this.begin < this.end) {
//			final boolean beginOk = CorpusPreprocessor.isWord(text.charAt(this.begin))
//					&& (this.begin == 0 || CorpusPreprocessor.isDelim(text.charAt(this.begin - 1)));
//			final boolean endOk = CorpusPreprocessor.isWord(text.charAt(this.end - 1))
//					&& (this.end == text.length() || CorpusPreprocessor.isDelim(text.charAt(this.end)));
//			if (!beginOk || !endOk) {
//				LOGGER.warn("Wrong span detected in " + documentURI + ": ..."
//						+ text.substring(Math.max(0, this.begin - 10), this.begin) + "["
//						+ text.substring(this.begin, this.end) + "]"
//						+ text.substring(this.end, Math.min(text.length(), this.end + 10))
//						+ "...");
//			}
//		}
//	}

	public boolean contains(final EasySpan span) {
		return this.begin <= span.begin && this.end >= span.end;
	}

	public boolean overlaps(final EasySpan span) {
		return this.end > span.begin && this.begin < span.end;
	}

	public List<EasySpan> split(final Iterable<EasySpan> spans) {

		final List<EasySpan> sortedSpans = Lists.newArrayList(spans);
		boolean overlaps = true;
		while (overlaps) {
			overlaps = false;
			Collections.sort(sortedSpans);
			for (int i = 0; i < sortedSpans.size() - 1; ++i) {
				final EasySpan span1 = sortedSpans.get(i);
				final EasySpan span2 = sortedSpans.get(i + 1);
				if (span1.end > span2.begin) {
					sortedSpans.remove(i);
					if (span1.begin < span2.begin) {
						sortedSpans.add(new EasySpan(span1.begin, span2.begin));
					}
					if (span1.end < span2.end) {
						sortedSpans.remove(i); // former i + 1
						sortedSpans.add(new EasySpan(span2.begin, span1.end));
						sortedSpans.add(new EasySpan(span1.end, span2.end));
					}
					else if (span1.end > span2.end) {
						sortedSpans.add(new EasySpan(span2.end, span1.end));
					}
					overlaps = true;
					// System.err.println(span1 + " " + span2 + " "
					// + new Span(span1.begin, span2.begin) + " "
					// + new Span(span2.begin, span1.end));
					break;
				}
			}
		}

		final List<EasySpan> result = Lists.newArrayList();
		int index = this.begin;
		for (final EasySpan span : sortedSpans) {
			if (span.begin < index) {
				throw new Error("Span overlap: " + spans);
			}
			if (span.begin > index) {
				result.add(new EasySpan(index, span.begin));
			}
			result.add(span);
			index = span.end;
		}
		if (index < this.end) {
			result.add(new EasySpan(index, this.end));
		}
		return result;
	}

	@Override
	public int compareTo(final EasySpan span) {
		int result = this.begin - span.begin;
		if (result == 0) {
			result = span.end - this.end;
		}
		return result;
	}

	@Override
	public boolean equals(final Object object) {
		if (object == this) {
			return true;
		}
		if (!(object instanceof EasySpan)) {
			return false;
		}
		final EasySpan other = (EasySpan) object;
		return this.begin == other.begin && this.end == other.end;
	}

	@Override
	public int hashCode() {
		return this.begin * 37 + this.end;
	}

	@Override
	public String toString() {
		return this.begin + "," + this.end;
	}

}
