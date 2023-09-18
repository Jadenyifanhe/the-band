import React from "react";

import "./App.css";
import { DisplayPlugin, MelodyState, Track } from "./melody";
import { TokenOption } from "./melody";

/**
 * Define the type of the props field for a React component
 */
interface Props {}

class App extends React.Component<Props, MelodyState> {
  private initialized: boolean = false;

  /**
   * @param props has type Props
   */
  constructor(props: Props) {
    super(props);
    /**
     * state has type MelodyState as specified in the class inheritance.
     */
    this.state = {
      trackData: [],
      dataPlugins: [],
      displayPlugins: [],
      tokenOption: "default",
      customToken: "",
      stage: "SELECT_DATA_PLUGIN",
      selectedDataPlugin: 0,
      selectedDisplayPlugin: 0,
      selectedDisplayPluginComponent: null,
      message: null,
    };
  }

  /**
   * This function is called when the component is loaded to tell the backend that
   * a new round is activated and get data plugin list from backend.
   */
  async start() {
    const response = await fetch("newMelody");
    console.log(response);
    const json = await response.json();
    this.setState({
      dataPlugins: json["plugins"].map(
        (plugin: { name: string }) => plugin.name
      ),
    });
  }

  /**
   * Updates the state with the data plugin index selected by the user
   * @param e event object
   */
  selectDataPlugin = (e: any) => {
    console.log(e.target.selectedIndex);
    this.setState({ selectedDataPlugin: e.target.selectedIndex });
  };

  /**
   * Send the chosen data pllugin to the server and update the stage
   * @returns {React.MouseEventHandler} - A function that can be invoked as an event handler
   */
  handleChooseDataPlugin(): React.MouseEventHandler {
    console.log("handleChooseDataPlugin");
    return async (e) => {
      e.preventDefault();
      const response = await fetch(
        `/dataPlugin?i=${this.state.selectedDataPlugin}`
      );
      const json = await response.json();
      console.log(json);
      this.setState({
        stage: json["stage"],
      });
    };
  }

  /**
   * Send the token to the server and update the stage
   * @returns {React.MouseEventHandler} - A function that can be invoked as an event handler
   */
  handleSetToken(): React.MouseEventHandler {
    return async () => {
      let response;
      if (this.state.tokenOption === "default") {
        response = await fetch(`/getAccessToken?method=default&token=`);
      } else if (this.state.tokenOption === "custom") {
        response = await fetch(
          `/getAccessToken?method=custom&token=${this.state.customToken}`
        );
      } else {
        response = await fetch(`/getAccessToken?method=browser&token=`);
      }
      const json = await response.json();
      this.setState({
        stage: json["stage"],
      });
    };
  }

  /**
   * Updates the state with the token method index selected by the user
   * @param e event object
   */
  handleTokenOptionChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    this.setState({ tokenOption: e.target.value as TokenOption });
  };

  /**
   * Updates the state with the updated custom token entered by the user
   * @param e event object
   */
  handleCustomTokenChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    this.setState({ customToken: e.target.value });
  };

  /**
   * Updates the state with the display plugin index selected by the user
   * @param e event object
   */
  selectDisplayPlugin = (e: any) => {
    console.log(e.target.selectedIndex);
    this.setState({ selectedDisplayPlugin: e.target.selectedIndex });
  };

  /**
   * Get the data from the server and load the chosen display plugin
   * @returns {React.MouseEventHandler} - A function that can be invoked as an event handler
   */
  handleChooseDisplayPlugin(): React.MouseEventHandler {
    return async () => {
      this.setState({
        message:
          "Please be patient while the plugin is loading data from the API. This might take a while. Please don't do any operations on the interface.",
      });
      const response = await fetch("/showDisplay");
      console.log(response);
      const json = await response.json();
      console.log(json);

      this.setState({
        stage: json["stage"],
        trackData: json["trackData"],
      });

      const { displayPlugins, selectedDisplayPlugin } = this.state;
      const displayPlugin = displayPlugins[selectedDisplayPlugin];
      console.log(displayPlugin);
      const path = `./displayPlugins/${displayPlugin}Plugin.tsx`;
      const plugin: DisplayPlugin = await import("" + path);
      console.log(this.state.trackData);
      this.setState({
        selectedDisplayPluginComponent: (
          <plugin.default data={json["trackData"]} />
        ),
      });
    };
  }

  /**
   * Restart the program by fetching the new melody data from the server.
   * @returns {React.MouseEventHandler} - A function that can be invoked as an event handler
   */
  handleRestart(): React.MouseEventHandler {
    return async () => {
      const response = await fetch("/newMelody");
      const json = await response.json();
      this.setState({
        stage: json["stage"],
        trackData: json["trackData"],
        dataPlugins: json["plugins"].map(
          (plugin: { name: string }) => plugin.name
        ),
        // displayPlugins: [],
        tokenOption: "default",
        customToken: "",
        selectedDataPlugin: 0,
        selectedDisplayPlugin: 0,
        selectedDisplayPluginComponent: null,
        message: null,
      });
    };
  }

  /**
   * This function will call after the HTML is rendered.
   * We load the display plugins registered in the txt file and initialize a new round.
   */
  async componentDidMount() {
    // load the display plugin
    const response = await fetch("./displayPlugins.txt");
    const text = await response.text();
    const displayPlugins = text
      .split("\n")
      .map((path) => path.trim())
      .filter((path) => path !== "");

    this.setState({ displayPlugins });

    /**
     * setState in DidMount() will cause it to render twice which may cause
     * this function to be invoked twice. Use initialized to avoid that.
     */
    if (!this.initialized) {
      this.start();
      this.initialized = true;
      this.setState({ displayPlugins });
    }
  }

  render(): React.ReactNode {
    const {
      dataPlugins,
      displayPlugins,
      selectedDisplayPluginComponent,
      stage,
      tokenOption,
      customToken,
      message,
    } = this.state;

    return (
      <div className="select-container">
        {stage === "SELECT_DATA_PLUGIN" && (
          <>
            <label htmlFor="dataSource">Choose data source:</label>
            <select
              name="dataSource"
              id="dataSource"
              onChange={this.selectDataPlugin}
            >
              {dataPlugins.map((plugin, index) => (
                <option key={index} value={plugin}>
                  {plugin}
                </option>
              ))}
            </select>
            <br />
            <button onClick={this.handleChooseDataPlugin()}>Next</button>
          </>
        )}

        {stage === "ENTER_USER_ACCESS_TOKEN" && (
          <>
            <label>
              <input
                type="radio"
                value="default"
                checked={tokenOption === "default"}
                onChange={this.handleTokenOptionChange}
              />
              Use default token
            </label>
            <br />
            <label>
              <input
                type="radio"
                value="custom"
                checked={tokenOption === "custom"}
                onChange={this.handleTokenOptionChange}
              />
              Enter token by user
            </label>
            {tokenOption === "custom" && (
              <input
                type="text"
                value={customToken}
                onChange={this.handleCustomTokenChange}
                placeholder="Enter your token"
              />
            )}
            <br />
            <label>
              <input
                type="radio"
                value="browser"
                checked={tokenOption === "browser"}
                onChange={this.handleTokenOptionChange}
              />
              Retrieve token by browser
            </label>
            <button onClick={this.handleSetToken()}>Next</button>
          </>
        )}

        {stage === "SELECT_DISPLAY_PLUGIN" && (
          <>
            {displayPlugins.length > 0 && (
              <>
                <label htmlFor="visualization">
                  Choose visualization type:
                </label>
                <select
                  name="displayOptions"
                  id="displayOptions"
                  onChange={this.selectDisplayPlugin}
                >
                  {displayPlugins.map((path, index) => (
                    <option key={index} value={index}>
                      {path}
                    </option>
                  ))}
                </select>
                <button onClick={this.handleChooseDisplayPlugin()}>Next</button>
                <p className="message">{message}</p>
              </>
            )}
          </>
        )}

        {stage === "DISPLAY" && selectedDisplayPluginComponent}

        <br />
        <button onClick={this.handleRestart()}>Restart</button>
      </div>
    );
  }
}

export default App;
