interface MelodyState {
  trackData: Track[];
  dataPlugins: string[];
  displayPlugins: string[];
  tokenOption: TokenOption;
  customToken: string;
  stage: string;
  selectedDataPlugin: number;
  selectedDisplayPlugin: number;
  selectedDisplayPluginComponent: React.ReactNode | null;
  message: string | null;
}

type TokenOption = "default" | "custom" | "browser";

interface Track {
  title: string;
  artist: string;
  timestamp: string;
  genre: string[];
  score: number[];
}

interface DisplayPlugin {
  default: React.ComponentType<any>;
}

export type { MelodyState, Track, DisplayPlugin, TokenOption };
